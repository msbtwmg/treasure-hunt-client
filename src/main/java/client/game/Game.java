package client.game;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.AI.PathFinder;
import client.game.model.ClientGameState;
import client.game.model.EClientGameState;
import client.game.model.EClientMove;
import client.map.model.ClientFullMapNode;
import client.map.model.ClientHalfMapNode;
import client.map.model.EClientTerrain;
import client.network.ClientNetwork;
import client.network.converter.GameStateConverter;
import client.network.converter.HalfMapConverter;


public class Game {

	    private static final Logger logger = LoggerFactory.getLogger(Game.class);

	    private final ClientNetwork network;
	    private final String playerId;
	    private final List<ClientHalfMapNode> halfMap;

	    private final PathFinder pathFinder = new PathFinder();

	    private volatile boolean running = true;
	    private final AtomicBoolean started = new AtomicBoolean(false);

	    private boolean sentMap = false;
	    private boolean moveSentForCurrentState = false;

	    private static final long MIN_INTERVAL = 400L;
	    private long lastRequest = 0;

	    private int moveCount = 0;
	    private static final int MAX_MOVES = 150;

	    private int lastX = -1;
	    private int lastY = -1;

	    public Game(ClientNetwork network, String playerId, List<ClientHalfMapNode> halfMap) {

	        this.network = network;
	        this.playerId = playerId;
	        this.halfMap = halfMap;
	    }

	    public void play() {

	        if (!started.compareAndSet(false, true)) {
	            logger.warn("Game already started");
	            return;
	        }

	        logger.info("Starting game loop");

	        while (running) {
	            try {

	            	long now = System.currentTimeMillis();
	     	        long diff = now - lastRequest;

	     	        if (lastRequest != 0 && diff < MIN_INTERVAL) {
	     	            Thread.sleep(MIN_INTERVAL - diff);
	     	        }


	                ClientGameState gameState = network.getGameState(playerId);
	                


	                moveSentForCurrentState = false;
	     	        lastRequest = System.currentTimeMillis();


	                handleGameState(gameState);

	            } catch (Exception e) {
	                logger.error("Error in game loop", e);
	                running = false;
	                System.exit(0);
	            }
	        }

	        logger.info("Play loop ended, client stopped.");
	    }

	    private void handleGameState(ClientGameState gameState) {

	        EClientGameState myState = gameState.getMyGameState();

	        if (myState == null) return;

	        if (myState == EClientGameState.Won || myState == EClientGameState.Lost) {
	            logger.info("Game finished: {}", myState);
	            running = false;
	            return;
	        }

	        if (myState == EClientGameState.MustAct) {
	            act(gameState);
	        }
	    }

	    private void act(ClientGameState gameState) {

	        long start = System.currentTimeMillis();   

	       
	        if (moveCount > MAX_MOVES) {
	            logger.error("Too many moves, STOPPING");
	            running = false;
	            return;
	        }

	        // Sending the half map ONCE
	        if (!sentMap) {
	            sendMap();
	            logger.debug("Map sent ({} ms)", System.currentTimeMillis() - start);
	            return;
	        }

	      
	        if (gameState.getMap() == null) {
	            logger.debug("FullMap not available yet");
	            return;
	        }

	        // Updating Pathfinder 
	        pathFinder.setNodes(gameState.getMap().getMapNodes());

	        ClientFullMapNode me = pathFinder.findMyPosition();
	        if (me == null) {
	            logger.warn("My position unknown");
	            return;
	        }

	        int x = me.getX();
	        int y = me.getY();

	        if (x == lastX && y == lastY) {
	            logger.debug("Still on same tile (possible stuck)");
	        }

	        
	        EClientMove move = pathFinder.decideNextMove(gameState);

	      
	        if (move == null || !isMoveSafe(gameState, me, move)) {
	            move = pathFinder.fallbackMove(me);
	        }

	        if (move == null || !isMoveSafe(gameState, me, move)) {
	            logger.warn("No safe move possible");
	            return;
	        }

	        lastX = x;
	        lastY = y;

	     
	        sendMove(move);

	        logger.info("Has treasure in inventory: {}", gameState.collectedTreasure());
	        long duration = System.currentTimeMillis() - start;

	        logger.debug("Decision took {} ms", duration);

	        if (duration > 4500) {
	            logger.warn("Close to timeout!!!");
	        }
	    }
	    
	    
	    private void sendMap() {
	        if (sentMap) return;

	        logger.info("Sending HalfMap");

	        network.sendHalfMap(
	            HalfMapConverter.toServerHalfMap(playerId, halfMap)
	        );

	        sentMap = true;
	    }

	    private void sendMove(EClientMove move) {

	        if (moveSentForCurrentState) return;

	        moveSentForCurrentState = true;
	        moveCount++;

	        network.sendMove(
	            GameStateConverter.toServerClientMove(playerId, move)
	        );

	        logger.debug("Move: {}", move);
	    }

	    private boolean isMoveSafe(ClientGameState gameState, ClientFullMapNode me,  EClientMove move) {
		
			int x = me.getX();
			int y = me.getY();
			
			int nx = x;
			int ny = y;
			
			switch (move) {
			case Right -> nx++;
			case Left  -> nx--;
			case Down  -> ny++;
			case Up    -> ny--;
			}
			
			ClientFullMapNode target = pathFinder.getNode(nx, ny);
			
			return target != null && target.getTerrain() != EClientTerrain.Water;
		}

	  
	
}

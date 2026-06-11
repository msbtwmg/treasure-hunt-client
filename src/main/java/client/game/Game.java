package client.game;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.AI.PathFinder;
import client.game.model.ClientGameState;
import client.game.model.EClientMove;
import client.map.HalfMapCreator;
import client.map.model.ClientFullMap;
import client.map.model.ClientFullMapNode;
import client.map.model.ClientHalfMap;
import client.map.model.ClientHalfMapNode;
import client.map.placement.DefaultFortPlacement;
import client.map.placement.FarFortPlacement;
import client.map.placement.FirstHalfPosition;
import client.map.placement.FirstHalfPositionFinder;
import client.map.placement.FortPlacementStrategy;
import client.map.placement.FortQuarter;
import client.map.rules.BorderAccessibilityRule;
import client.map.rules.FortRule;
import client.map.rules.MapConnectionRule;
import client.map.rules.MapRule;
import client.map.rules.RuleError;
import client.map.rules.RuleNotifications;
import client.map.rules.TerrainRule;
import client.network.ClientNetwork;
import client.network.converter.GameStateConverter;
import client.network.converter.HalfMapConverter;
import exceptions.HalfMapGenerationException;
import exceptions.UnsafeMoveException;


public class Game {

	    private static final Logger logger = LoggerFactory.getLogger(Game.class);

	    private final ClientNetwork network;
	    private final String playerId;
	    private List<ClientHalfMapNode> halfMap;

	    private final PathFinder pathFinder = new PathFinder();

	    private volatile boolean running = true;
	    private final AtomicBoolean started = new AtomicBoolean(false);

	    private boolean sentMap = false;
	    private boolean moveSentForCurrentState = false;

	    private static final long MIN_INTERVAL = 400L;
	    private long lastRequest = 0;

	    private int moveCount = 0;
	    private static final int MAX_MOVES = 360;

	    private int lastX = -1;
	    private int lastY = -1;
	    
	    private final GameController controller;
	    

	    public Game(ClientNetwork network, String playerId, GameController controller) {

	        this.network = network;
	        this.playerId = playerId;
	        this.controller = controller; 
	     
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
	                controller.receivedGameState(gameState);
	             
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

	        if (gameState == null || gameState.getMyGameState() == null) return;

	        if (gameState.gameFinished()) {
	            logger.info("Game finished: {}", gameState.getMyGameState());
	            running = false;
	            return;
	        }

	        if (gameState.mustAct()) {
	            act(gameState);
	        }
	    }
	    
	    private void act(ClientGameState gameState) {

	        long start = System.currentTimeMillis();   

	       
	        if (tooManyMoves()) return;

	        if (!sentMap) {
	        	try {
					createHalfMap(gameState);
		            sendMap();
	        	} catch (HalfMapGenerationException e) {
	        		
	        		logger.error("Couldnt create half map", e);
	        		running = false;
		            return;
	        	}
	        	logger.debug("Map sent ({} ms)", System.currentTimeMillis() - start);
	        	return;
	        }
	        

	        if (!preparePathFinder(gameState)) return;
	        
	        ClientFullMap map = gameState.getMap(); 

	        ClientFullMapNode me = map.findMyPosition();
	        
	        if (me == null) {
	            logger.warn("My position unknown");
	            return;
	        }

	        rememberPosition(me);
	        
	        EClientMove move;
	        
	        try {
	        	move = chooseMove(gameState, me);
	        } catch (UnsafeMoveException e) {
	        	logger.error("No safe move possible", e);
	        	running = false;
	        	return;
	        }
	        
	        sendMove(move);

	        logDecisionTime(start, gameState.collectedTreasure());

	    }
	    
	    private void logDecisionTime(long start, boolean hasTreasure) {
	        logger.info("Has treasure in inventory: {}", hasTreasure);

	        long duration = System.currentTimeMillis() - start;
	        logger.debug("Decision took {} ms", duration);

	        if (duration > 4500) {
	            logger.warn("Close to timeout!!!");
	        }
	    }
	    
	    private EClientMove chooseMove(ClientGameState gameState, ClientFullMapNode me) {
	        boolean hasTreasure = gameState.collectedTreasure();

	        EClientMove move = pathFinder.decideNextMove(hasTreasure);

	        if (move == null || !isMoveSafe(gameState, me, move)) {
	            move = pathFinder.fallbackMove(me, hasTreasure);
	        }

	       /* if (move == null || !isMoveSafe(gameState, me, move)) {
	            return null;
	        } */

	        return move;
	    }
	    
	    private void rememberPosition(ClientFullMapNode me) {
	        int x = me.getX();
	        int y = me.getY();

	 
	        lastX = x;
	        lastY = y;
	    }
	    
	    
	    private boolean preparePathFinder(ClientGameState gameState) {
	        if (!gameState.hasFullMap()) {
	            logger.debug("FullMap not available yet");
	            return false;
	        }

	        pathFinder.setMap(gameState.getMap());
	        return true;
	    }
	    
	    private boolean tooManyMoves() { 
	        if (moveCount <=  MAX_MOVES) return false; 
	            logger.error("Too many moves, STOPPING");
	            running = false;
	            return true;

	    }
	    
	    private void sendMap() {
	        if (sentMap) return;

	        if (!validateHalfMap()) {
		        logger.error("HalfMap validation failed");
		        return;
		    }
	        
	        logger.info("Sending HalfMap");

	        network.sendHalfMap(
	            HalfMapConverter.toServerHalfMap(playerId, halfMap)
	        );

	        sentMap = true;
	    }
	    
	    private void createHalfMap(ClientGameState gameState) throws HalfMapGenerationException {
	        if (halfMap != null) return;


	        FortPlacementStrategy strategy = chooseFortStrategy(gameState);

	        HalfMapCreator creator = new HalfMapCreator(strategy);
	        halfMap = creator.createHalfMap();
	    }
	    
	    private FortPlacementStrategy chooseFortStrategy(ClientGameState gameState) {

	        if (!gameState.isSecondClient()) {
	            logger.info("First client -> using default fort placement");
	            return new DefaultFortPlacement();
	        }

	        FirstHalfPosition firstHalfPosition =  new FirstHalfPositionFinder().find(gameState.getMapNodes());

	        logger.info("Second client -> first half is {}", firstHalfPosition);

	        return switch (firstHalfPosition) {
	            case ORIGIN -> new FarFortPlacement(FortQuarter.BOTTOM_RIGHT);
	            case RIGHT  -> new FarFortPlacement(FortQuarter.BOTTOM_LEFT);
	            case BOTTOM -> new FarFortPlacement(FortQuarter.TOP_RIGHT);
	            case NOTFOUND -> new DefaultFortPlacement();
	        };
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

	    private boolean isMoveSafe(ClientGameState gameState, ClientFullMapNode me, EClientMove move) {
	        ClientFullMap map = gameState.getMap();
	        return map.isMoveSafe(me, move);
	    }
	    
	    private boolean validateHalfMap() {

	        if (halfMap == null || halfMap.isEmpty()) {
	            logger.error("HalfMap is empty");
	            return false;
	        }

	        ClientHalfMap map = new ClientHalfMap(Set.copyOf(halfMap));

	        RuleNotifications notifications = new RuleNotifications();

	        List<MapRule> rules = List.of(
	                new BorderAccessibilityRule(),
	                new TerrainRule(24, 7, 6),
	                new MapConnectionRule(),
	                new FortRule()
	        );

	        for (MapRule rule : rules) {
	            rule.validate(map, notifications);
	        }

	        if (notifications.hasError()) {
	            for (RuleError error : notifications.getErrors()) {
	                logger.error("Map validation error: {}", error.getMessage());
	            }

	            return false;
	        }

	        return true;
	    }

}

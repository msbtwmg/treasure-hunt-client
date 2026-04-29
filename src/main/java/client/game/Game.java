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
    private boolean moveSent = false;

    private static final long MIN_INTERVAL = 400;
    private long lastRequest = 0;

    public Game(ClientNetwork network,
                String playerId,
                List<ClientHalfMapNode> halfMap) {

        this.network = network;
        this.playerId = playerId;
        this.halfMap = halfMap;
    }

    /** 
     * Main Game Loop
     */
    public void play() {

        if (!started.compareAndSet(false, true)) {
            logger.warn("Game already started");
            return;
        }

        logger.info("Starting game loop");

        while (running) {
            try {

                throttle();

                ClientGameState gameState = network.getGameState(playerId);

                moveSent = false; // reset per state

                handleGameState(gameState);

            } catch (Exception e) {
                logger.error("Error in game loop", e);
                running = false;
            }
        }

        logger.info("Game stopped.");
    }

    /**
     * GameState handling
     */
    private void handleGameState(ClientGameState gameState) {

        EClientGameState myState = gameState.getMyGameState();

        if (myState == null) {
            logger.debug("My GameState not available yet");
            return;
        }

        if (myState == EClientGameState.Won || myState == EClientGameState.Lost) {
            logger.info("Game finished for me: {}", myState);
            running = false;
            return;
        }

        if (myState == EClientGameState.MustAct) {
            act(gameState);
        } else {
            logger.debug("~ Waiting {}", myState);
        }
    }

    /**
     * Main action logic (map + moves)
     */
    private void act(ClientGameState gameState) {

        // send map once
        if (!sentMap) {
            sendMapIfNeeded();
            return;
        }

        // update PathFinder with current map
        pathFinder.setNodes(gameState.getMap().getMapNodes());

        // decide next move
        EClientMove move = pathFinder.decideNextMove(gameState);

        if (move == null) {
            logger.warn("No move decided");
            return;
        }

        sendMove(move);
    }

    /**
     * Send map once
     */
    private void sendMapIfNeeded() {

        if (sentMap) return;

        logger.info("~ Sending HalfMap");

        network.sendHalfMap(
                HalfMapConverter.toServerHalfMap(playerId, halfMap)
        );

        sentMap = true;
    }

    /**
     * Send move once per GameState
     */
    private void sendMove(EClientMove move) {

        if (moveSent) return;

        moveSent = true;

        network.sendMove(
                GameStateConverter.toServerClientMove(playerId, move)
        );

        logger.debug("Move sent: {}", move);
    }

    /**
     * Timing control
     */
    private void throttle() throws InterruptedException {

        long now = System.currentTimeMillis();
        long diff = now - lastRequest;

        if (lastRequest != 0 && diff < MIN_INTERVAL) {
            Thread.sleep(MIN_INTERVAL - diff);
        }

        lastRequest = System.currentTimeMillis();
    }
}
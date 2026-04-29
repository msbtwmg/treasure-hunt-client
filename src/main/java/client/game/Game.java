package client.game;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.game.model.ClientGameState;
import client.game.model.EClientGameState;
import client.map.model.ClientHalfMapNode;
import client.network.ClientNetwork;
import client.network.converter.HalfMapConverter;

public class Game {

    private static final Logger logger = LoggerFactory.getLogger(Game.class);

    private final ClientNetwork network;
    private final String playerId;
    private final List<ClientHalfMapNode> halfMap;

    private volatile boolean running = true;
    private final AtomicBoolean started = new AtomicBoolean(false);

    private boolean sentMap = false;

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

        logger.info("Starting minimal game loop (map only)");

        while (running) {
            try {

                throttle();

                ClientGameState gameState = network.getGameState(playerId);

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
     * @param gameState
     */

    private void handleGameState(ClientGameState gameState) {

        EClientGameState myState = gameState.getMyGameState();

        if (myState == null) {
            logger.debug("GameState not ready yet");
            return;
        }

        if (myState == EClientGameState.Won || myState == EClientGameState.Lost) {
            logger.info("Game finished: {}", myState);
            running = false;
            return;
        }

        if (myState == EClientGameState.MustAct) {
            sendMapIfNeeded();
        } else {
            logger.debug("~ Waiting {}", myState);
        }
    }

    /**
     * Makes sure the map is only sent once. 
     */
    private void sendMapIfNeeded() {

        if (sentMap) return;

        logger.info("~Sending HalfMap");

        network.sendHalfMap(
                HalfMapConverter.toServerHalfMap(playerId, halfMap)
        );

        sentMap = true;
    }

    /**
     * Timing
     * @throws InterruptedException
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
package client.main;

import client.game.Game;
import client.map.HalfMapCreator;
import client.map.model.ClientHalfMapNode;
import client.network.ClientNetwork;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exceptions.InvalidArgumentsException;

public class MainClient {
	
	private static Logger logger = LoggerFactory.getLogger(MainClient.class);
	
    private static String FIRSTNAME = "REDACTED";
    private static String LASTNAME  = "REDACTED";
    private static String USERNAME  = "REDACTED";
    

	public static void main(String[] args) {

        try {
            if (args.length < 3) {
                throw new InvalidArgumentsException(
                        "Not enough start parameters: " + args.length);
            }

            final String mode = args[0];
            final String serverBaseUrl = args[1];
            final String gameId = args[2];

            /**
             * Creating config
             */
            ClientConfig config = new ClientConfig(
                    mode,
                    serverBaseUrl,
                    gameId,
                    FIRSTNAME,
                    LASTNAME,
                    USERNAME
            );

            /**
             * Registering the player
             */
            ClientNetwork network = new ClientNetwork(config);
            String playerId = network.registerPlayer().getUniquePlayerID();

            logger.info("Registered player: {}", playerId);

            /**
             * Creating the map
             */
            HalfMapCreator creator = new HalfMapCreator();
            List<ClientHalfMapNode> nodes = creator.createHalfMap();

            logger.info("HalfMap successfully created");

            /**
             * Starting the game loop
             * (Game class handles sending the map)
             */
            Game game = new Game(network, playerId, nodes);

            logger.info("Starting game...");
            game.play();

        }
        catch (InvalidArgumentsException e) {
            logger.error("Invalid Arguments: {}", e.getMessage());
        }
        catch (Exception e) {
            logger.error("Fatal error in main", e);
            System.exit(1); 
        }
	}
}
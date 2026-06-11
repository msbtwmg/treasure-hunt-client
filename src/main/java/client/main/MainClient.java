package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.game.Game;
import client.game.GameController;
import client.game.GameModel;
import client.game.StateView;
import client.game.ValidationView;
import client.map.MapView;
import client.network.ClientNetwork;
import exceptions.UnsafeMoveException;




public class MainClient {

	private static final Logger logger = LoggerFactory.getLogger(MainClient.class);
	
    private static String FIRSTNAME = "REDACTED";
    private static String LASTNAME  = "REDACTED";
    private static String USERNAME  = "REDACTED";
    

	public static void main(String[] args) {
	   
        try {
            if (args.length < 3) {
                throw new UnsafeMoveException(
                        "Not enough start parameters: " + args.length);
            }

            final String mode = args[0];
            final String serverBaseUrl = args[1];
            final String gameId = args[2];

         
            ClientConfig config = new ClientConfig(
                    mode,
                    serverBaseUrl,
                    gameId,
                    FIRSTNAME,
                    LASTNAME,
                    USERNAME
            );

           
            ClientNetwork network = new ClientNetwork(config);
            String playerId = network.registerPlayer().getUniquePlayerID();

            logger.info("Registered player: {}", playerId); 
            
            GameModel model = new GameModel(); 
            GameController controller = new GameController(model);
            
            new MapView(model);
            new StateView(model);
            new ValidationView(model);
            
            Game game = new Game(network, playerId, controller);
      

            logger.info("~~~Starting game~~~~");
            game.play();

        }
        catch (UnsafeMoveException e) {
            logger.error("Invalid Arguments: {}", e.getMessage());
        }
        catch (Exception e) {
            logger.error("Fatal error in main", e);
        }
	}
	
	
}
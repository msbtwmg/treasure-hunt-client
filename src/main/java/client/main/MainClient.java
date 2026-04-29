package client.main;


import client.network.ClientNetwork;


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
            
            // TODO: vllt factory pattern um new loszuwerden? 

            ClientConfig config =
                    new ClientConfig(mode, serverBaseUrl, gameId,
                                     FIRSTNAME, LASTNAME, USERNAME);
            
            ClientNetwork network = new ClientNetwork(config); 
            network.registerPlayer();

        }
        catch (InvalidArgumentsException e) {
            logger.error("Invalid Arguments: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Fatal error in main", e);
        }
        
	}
	
	
	
}
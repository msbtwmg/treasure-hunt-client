package client.main;


import client.map.HalfMapCreator;
import client.map.model.ClientHalfMapNode;
import client.network.ClientNetwork;
import client.network.converter.HalfMapConverter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exceptions.InvalidArgumentsException;
import messagesbase.messagesfromclient.PlayerHalfMap;


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
            
     

            ClientConfig config =  new ClientConfig(mode, serverBaseUrl, gameId, FIRSTNAME, LASTNAME, USERNAME);
            
            /** 
             * Registering the player
             */
            ClientNetwork network = new ClientNetwork(config); 
            String playerId = network.registerPlayer().getUniquePlayerID();
            
            /** 
             * 
             *Creating the map
             */
            HalfMapCreator creator = new HalfMapCreator();
            List<ClientHalfMapNode> nodes = creator.createHalfMap();
            
            /**
             * Convert and send the map for the server
             */
            PlayerHalfMap halfMap = HalfMapConverter.toServerHalfMap(playerId, nodes);
            

            network.sendHalfMap(halfMap);


        }
        catch (InvalidArgumentsException e) {
            logger.error("Invalid Arguments: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Fatal error in main", e);
        }
        
	}
	
	
	
}
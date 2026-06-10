package client.network.converter;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import client.game.model.EClientMove;
import client.map.model.EClientTerrain;
import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;

class GameStateConverter_Tests {

	//TODO: Arrange, Arrange, Assert beachten
	
	 @ParameterizedTest
	    @CsvSource({
	        "Grass, Grass",
	        "Water, Water",
	        "Mountain, Mountain"
	    })
	    public void terrainConverter_shouldConvertToClientTerrain(ETerrain serverTerrain, 
	    		EClientTerrain expectedTerrain) {
		 
	        EClientTerrain result =  GameStateConverter.toClientTerrain(serverTerrain);
	       
	      assertEquals(expectedTerrain, result);

	    }
	 
	 @ParameterizedTest
	    @CsvSource({
	        "Up, Up",
	        "Down, Down",
	        "Left, Left",
	        "Right, Right"
	    })
	    public void moveConverter_shouldConvertToClientMove(EMove serverMove ,
	    		EClientMove expectedMove) {

	        EClientMove result =
	                GameStateConverter.toClientMove(serverMove);
	       
	       assertEquals(expectedMove, result);

	    }
	 
	 
	 @ParameterizedTest
	    @CsvSource({
	        "Up, Up",
	        "Down, Down",
	        "Left, Left",
	        "Right, Right"
	    })
	    public void moveConverter_shouldConvertToServerMove(EClientMove clientMove ,
	    		EMove expectedMove) {

	        EMove result =
	                GameStateConverter.toServerMove(clientMove);
	       
	       assertEquals(expectedMove, result);

	    }
	 

	 @Test
	 public void clientMove_shouldCreateCorrectPlayerMove() {
		 String testPlayerId = "TestPlayerId";
		 EClientMove move = EClientMove.Down;
		 
		 PlayerMove result = GameStateConverter.toServerClientMove(testPlayerId, move);
		 
		 assertEquals(testPlayerId, result.getUniquePlayerID());
		 assertEquals(EMove.Down, result.getMove());
	 }
	 
	 @Test
	 public void clientGameState_shouldConvertCollectedTreasure() {
		 String testPlayerId = "TestPlayerId";
		 EClientMove move = EClientMove.Down;
		 
		 PlayerMove result = GameStateConverter.toServerClientMove(testPlayerId, move);
		 
		 assertEquals(testPlayerId, result.getUniquePlayerID());
		 assertEquals(EMove.Down, result.getMove());
	 }
	 
	 
	 
}


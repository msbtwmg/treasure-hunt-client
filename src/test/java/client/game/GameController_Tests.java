package client.game;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import client.game.model.ClientGameState;
import client.game.model.EClientGameState;
import client.map.rules.RuleError;

public class GameController_Tests {

	  @Test
	    public void receivedGameState_withWonState_shouldbeFinishedWon() {

		  	GameModel model = mock(GameModel.class);
	        GameController controller = new GameController(model);
	        ClientGameState gameState = mock(ClientGameState.class);

	        when(gameState.getMyGameState()).thenReturn(EClientGameState.Won);

	        controller.receivedGameState(gameState);

	        verify(model).updateGameState(gameState);
	        verify(model).setGameFinished(true);
	    }
	  
	  @Test
	    public void receivedGameState_withLostState_shouldBeFinishedLost() {
	        
	        GameModel model = mock(GameModel.class);
	        GameController controller = new GameController(model);
	        ClientGameState gameState = mock(ClientGameState.class);

	        when(gameState.getMyGameState()).thenReturn(EClientGameState.Lost);

	        controller.receivedGameState(gameState);

	        verify(model).updateGameState(gameState);
	        verify(model).setGameFinished(false);
	    }
	  
	  @Test
	    public void mapValidationError_shouldNotifyModel() {
	        
	        GameModel model = mock(GameModel.class);
	        GameController controller = new GameController(model);
	       
	        controller.mapValidationError("No fort exists");
	        verify(model).notifyMapValidationError(any(RuleError.class));
	    }
	  
}

package client.game;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import client.game.model.ClientGameState;
import client.game.observer.GameObserver;

public class GameModel_Tests {

    @Test
    public void updateGameState_shouldNotifyObservers() {
        GameModel model = new GameModel();

        @SuppressWarnings("unchecked")
        GameObserver<ClientGameState> observer = mock(GameObserver.class);
        model.gameStateChanged.addObserver(observer);

        ClientGameState state = mock(ClientGameState.class);

        model.updateGameState(state);

        verify(observer).update(state);
    }
    
    @Test
    public void getFullMap_withouttGameState_shouldReturnNull() {
        GameModel model = new GameModel();

        assertNull(model.getFullMap());
    }
    
    
}
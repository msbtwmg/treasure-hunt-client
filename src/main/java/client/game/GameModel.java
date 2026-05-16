package client.game;

import client.game.model.ClientGameState;
import client.game.observer.GameEvent;
import client.map.model.ClientFullMap;
import client.map.rules.RuleError;

public class GameModel {

    private ClientGameState currentState;

    public final GameEvent<ClientGameState> gameStateChanged = new GameEvent<>();
    public final GameEvent<RuleError> mapValidationError = new GameEvent<>();
    private boolean gameFinished;
    private boolean won;


    public void updateGameState(ClientGameState newState) {
        this.currentState = newState;
        gameStateChanged.notifyObservers(newState);
    }
    

    public ClientGameState getCurrentState() {
        return currentState;
    }

    public ClientFullMap getFullMap() {
        return currentState == null ? null : currentState.getMap();
    }

    public boolean hasCollectedTreasure() {
        return currentState != null && currentState.collectedTreasure();
    }
    
    public void notifyMapValidationError(RuleError errorMessage) {
        mapValidationError.notifyObservers(errorMessage);
    }
    
  
    public final GameEvent<Boolean> gameFinishedChanged = new GameEvent<>();
    

    public void setGameFinished(boolean won) {
        this.gameFinished = true;
        this.won = won;
        gameFinishedChanged.notifyObservers(won);
    }
    
    
}
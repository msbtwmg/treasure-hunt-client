package client.game;

import client.game.model.ClientGameState;
import client.game.model.EClientGameState;
import client.map.rules.RuleError;

public class GameController {

    private final GameModel model;

    public GameController(GameModel model) {
        this.model = model;
    }

    public void receivedGameState(ClientGameState gameState) {
        model.updateGameState(gameState);

        if (gameState.getMyGameState() == EClientGameState.Won ||
            gameState.getMyGameState() == EClientGameState.Lost) {

            boolean won = gameState.getMyGameState() == EClientGameState.Won;
            model.setGameFinished(won);
        }
    }

    public void mapValidationError(String errorInfo) {
        model.notifyMapValidationError(new RuleError(errorInfo));
    }
}
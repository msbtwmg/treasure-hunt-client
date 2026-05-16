package client.game;

import client.map.rules.RuleError;

public class ValidationView {

    public ValidationView(GameModel model) {
        model.mapValidationError.addObserver(error-> renderError(error));
    }

    private void renderError(RuleError error) {
        System.err.println("!! MAP VALIDATION ERROR !!");
        System.err.println(error.getMessage());
    }
}
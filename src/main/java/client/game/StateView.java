package client.game;

public class StateView {

    public StateView(GameModel model) {
        model.gameFinishedChanged.addObserver(won -> renderResult(won));
    }

    private void renderResult(boolean won) {
        if (won) {
            System.out.println("✨CONGRATULATIONS, YOU WON!✨");
        } else {
            System.out.println("😭OH NO, YOU LOST...😭");
        }
    }
}
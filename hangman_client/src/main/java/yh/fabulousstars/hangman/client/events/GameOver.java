package yh.fabulousstars.hangman.client.events;

public class GameOver extends AbstractEvent {
    private final boolean winner;

    public GameOver(boolean winner) {
        this.winner = winner;
    }

    public boolean isWinner() {
        return winner;
    }
}

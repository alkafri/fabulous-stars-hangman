package yh.fabulousstars.hangman.client.events;

public class LeaveGame extends AbstractEvent {
    private final String gameId;

    public LeaveGame(String gameId) {
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }
}

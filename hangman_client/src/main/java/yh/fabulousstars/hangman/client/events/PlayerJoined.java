package yh.fabulousstars.hangman.client.events;

public class PlayerJoined extends AbstractEvent {
    private final String clientId;
    private final String name;
    private final String gameId;

    public PlayerJoined(String clientId, String name, String gameId) {
        this.clientId = clientId;
        this.name = name;
        this.gameId = gameId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public String getGameId() {
        return gameId;
    }
}

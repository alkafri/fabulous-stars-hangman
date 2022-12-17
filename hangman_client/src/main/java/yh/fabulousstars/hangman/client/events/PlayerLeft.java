package yh.fabulousstars.hangman.client.events;

public class PlayerLeft extends AbstractEvent {
    private String clientId;

    public PlayerLeft(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}

package yh.fabulousstars.hangman.client.events;

import yh.fabulousstars.hangman.client.PlayState;

public class PlayerState extends AbstractEvent {
    private String clientId;
    private PlayState state;

    public PlayerState(String clientId, PlayState state) {

        this.clientId = clientId;
        this.state = state;
    }

    public PlayState getState() {
        return state;
    }

    public String getClientId() {
        return clientId;
    }
}

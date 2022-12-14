package yh.fabulousstars.hangman.client.events;

import yh.fabulousstars.hangman.client.IPlayer;

public class ClientConnect extends AbstractEvent {
    private final IPlayer player;
    private final String error;

    public ClientConnect(IPlayer player, String error) {
        this.player = player;
        this.error = error;
    }

    public IPlayer getPlayer() {
        return player;
    }

    public String getError() {
        return error;
    }
}

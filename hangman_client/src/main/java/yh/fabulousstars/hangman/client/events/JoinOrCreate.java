package yh.fabulousstars.hangman.client.events;

import yh.fabulousstars.hangman.client.IGame;

public class JoinOrCreate extends AbstractEvent{
    private final IGame game;
    private final String error;


    public JoinOrCreate(IGame game, String error) {
        this.game = game;
        this.error = error;
    }

    public IGame getGame() {
        return game;
    }

    public String getError() {
        return error;
    }
}

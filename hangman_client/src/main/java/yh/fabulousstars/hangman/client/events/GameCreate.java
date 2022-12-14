package yh.fabulousstars.hangman.client.events;

import yh.fabulousstars.hangman.client.IGame;

public class GameCreate extends AbstractEvent {
    private IGame game;
    private String error;

    public GameCreate(IGame game, String error) {
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

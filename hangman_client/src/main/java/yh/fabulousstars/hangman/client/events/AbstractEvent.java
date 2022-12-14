package yh.fabulousstars.hangman.client.events;

import yh.fabulousstars.hangman.client.IGame;
import yh.fabulousstars.hangman.client.IGameEvent;

public abstract class AbstractEvent implements IGameEvent {
    @Override
    public String getType() {
        return getClass().getSimpleName();
    }
}

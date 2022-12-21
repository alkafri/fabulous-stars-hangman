package yh.fabulousstars.hangman.client.events;

import yh.fabulousstars.hangman.client.IPlayer;

import java.util.List;

public class PlayerState extends AbstractEvent {
    private List<IPlayer> states;

    public PlayerState(List<IPlayer> states) {
        this.states = states;
    }

    public List<IPlayer> getStates() {
        return states;
    }
}

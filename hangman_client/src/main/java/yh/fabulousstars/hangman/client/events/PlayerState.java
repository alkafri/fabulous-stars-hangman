package yh.fabulousstars.hangman.client.events;

public class PlayerState extends AbstractEvent {
    public record State(
            String clientId
    ) {}
    private State state;

    public PlayerState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }
}

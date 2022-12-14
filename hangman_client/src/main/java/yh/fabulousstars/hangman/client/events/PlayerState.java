package yh.fabulousstars.hangman.client.events;

public class PlayerState extends AbstractEvent {
    private String clientId;
    private PlayerState state;
    private int damage;

    public PlayerState(String clientId, PlayerState state, int damage) {
        this.clientId = clientId;
        this.state = state;
        this.damage = damage;
    }
}

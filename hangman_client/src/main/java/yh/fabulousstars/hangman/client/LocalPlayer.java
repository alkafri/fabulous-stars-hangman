package yh.fabulousstars.hangman.client;

public class LocalPlayer implements IPlayer {
    private final String clientId;
    private final LocalGame game;
    private final String name;
    private PlayerState state;
    private int damage;

    LocalPlayer(LocalGame game, String name, String clientId) {
        this.clientId = clientId;
        this.game = game;
        this.name = name;
        this.state = PlayerState.Initial;
        this.damage = 0;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public IGame getGame() {
        return game;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PlayerState getState() {
        return state;
    }

    @Override
    public int getDamage() {
        return damage;
    }
}

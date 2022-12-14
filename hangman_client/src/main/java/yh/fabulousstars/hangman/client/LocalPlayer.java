package yh.fabulousstars.hangman.client;

class LocalPlayer implements IPlayer {
    private final String clientId;
    private final String name;

    private LocalGame game;
    private PlayerState state;
    private int damage;


    LocalPlayer(String name, String clientId) {
        this.clientId = clientId;
        this.game = null;
        this.name = name;
        this.state = PlayerState.WaitForStart;
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

    @Override
    public void submit(String value) {
        if(game != null) {
            game.getClient().submit(clientId, value);
        }
    }
}

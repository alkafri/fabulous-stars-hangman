package yh.fabulousstars.hangman.client;

class LocalPlayer implements IPlayer {
    private final String clientId;
    private final String name;

    private LocalGame game;
    private final GameManager manager;
    private PlayState playState;

    LocalPlayer(GameManager manager, LocalGame game, String name, String clientId) {
        this.manager = manager;
        this.clientId = clientId;
        this.game = game;
        this.name = name;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public IGame getGame() {
        return game;
    }

    void setGame(LocalGame game) {
        game = game;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PlayState getPlayState() {
        return playState;
    }

    public void setPlayState(PlayState playState) {
        this.playState = playState;
    }

    @Override
    public void submitWord(String value) {
        if (game != null) {
            manager.submitWord(value);
        }
    }

    @Override
    public void submitGuess(String value) {
        if (game != null) {
            manager.submitGuess(value);
        }
    }

    @Override
    public void say(String message) {
        manager.say(message);
    }
}

package yh.fabulousstars.hangman.client;

class LocalPlayer implements IPlayer {
    private final String clientId;
    private final String name;

    private LocalGame game;
    private PlayState playState;

    LocalPlayer(LocalGame game, String name, String clientId) {
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PlayState getPlayState() {
        return playState;
    }

    @Override
    public void submitWord(String value) {
        if(game != null) {
            game.getClient().submitWord(value);
        }
    }
    @Override
    public void submitGuess(String value) {
        if(game != null) {
            game.getClient().submitGuess(value);
        }
    }

    void setGame(LocalGame game) {
        game = game;
    }

    public void setPlayState(PlayState playState) {
        this.playState = playState;
    }
}

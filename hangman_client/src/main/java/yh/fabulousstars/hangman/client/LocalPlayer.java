package yh.fabulousstars.hangman.client;

import yh.fabulousstars.hangman.game.PlayState;

class LocalPlayer implements IPlayer {
    private final String clientId;
    private final String name;
    private final GameManager manager;
    private LocalGame game;
    private PlayState playState;

    LocalPlayer(GameManager manager, LocalGame game, String name, String clientId) {
        this.manager = manager;
        this.clientId = clientId;
        this.game = game;
        this.name = name;
    }

    @Override
    public IGameManager getManager() {
        return manager;
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
        this.game = game;
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
}

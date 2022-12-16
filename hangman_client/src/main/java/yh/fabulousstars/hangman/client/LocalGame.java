package yh.fabulousstars.hangman.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LocalGame implements IGame {
    private final String gameId;
    private GameManager manager;
    private String name;
    private List<LocalPlayer> players;

    LocalGame(GameManager manager, String gameId, String name) {
        this.gameId = gameId;
        this.manager = manager;
        this.name = name;
        players = new ArrayList<>();
    }

    GameManager getClient() {
        return manager;
    }

    @Override
    public String getId() {
        return gameId;
    }

    @Override
    public IGameManager getManager() {
        return manager;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void join(String password) {
        manager.join(gameId, password);
    }

    @Override
    public void leave() {
        manager.leave();
    }
}

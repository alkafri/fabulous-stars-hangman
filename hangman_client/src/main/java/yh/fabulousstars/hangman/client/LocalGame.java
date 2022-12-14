package yh.fabulousstars.hangman.client;

import java.util.ArrayList;
import java.util.List;

class LocalGame implements IGame {
    private final String gameId;
    private GameClient manager;
    private String name;
    private List<LocalPlayer> players;

    LocalGame(GameClient manager, String gameId, String name) {
        this.gameId = gameId;
        this.manager = manager;
        this.name = name;
        players = new ArrayList<>();
    }

    GameClient getClient() {
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
    public String getGameTheme() {
        return null;
    }

    @Override
    public List<IPlayer> getPlayers() {
        return new ArrayList<>(players);
    }

    @Override
    public void join(String password) {
        manager.join(gameId);
    }
}

package yh.fabulousstars.hangman.client;

import java.util.*;
import java.util.stream.Collectors;

class LocalGame implements IGame {
    private final String gameId;
    private final GameManager manager;
    private final String name;
    private final Map<String, LocalPlayer> players;

    LocalGame(GameManager manager, String gameId, String name) {
        this.gameId = gameId;
        this.manager = manager;
        this.name = name;
        players = new HashMap<>();

    }

    GameManager getClient() {
        return manager;
    }

    @Override
    public List<IPlayer> getPlayers() {
        return players.values().stream().collect(Collectors.toList());
    }

    @Override
    public IPlayer getPlayer(String clientId) {
        return players.get(clientId);
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
    public void leave() {
        manager.leave();
    }

    @Override
    public void start() {
        manager.start();
    }
}

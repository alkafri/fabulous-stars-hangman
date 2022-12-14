package yh.fabulousstars.hangman.client;

import yh.fabulousstars.hangman.client.events.FailedToJoin;
import yh.fabulousstars.hangman.client.events.PlayerJoined;

import java.util.ArrayList;
import java.util.List;

public class LocalGame implements IGame {
    private static long gameIdCounter = 100;
    private final long id;
    private GameClient manager;
    private String theme;
    private String name;
    private String password;
    private List<LocalPlayer> players;
    private IPlayer me;

    LocalGame(GameClient manager, String gameName, String playerName, String clientId, String password) {
        this.id = gameIdCounter++;
        this.manager = manager;
        this.name = gameName;
        this.password = password;
        this.theme = null;
        players = new ArrayList<>();
        me = new LocalPlayer(this, playerName, clientId);

    }

    @Override
    public long getId() {
        return id;
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
        return theme;
    }

    @Override
    public List<IPlayer> getPlayers() {
        return new ArrayList<>(players);
    }

    @Override
    public IPlayer getMe() {
        return me;
    }

    @Override
    public void joinGame(IPlayer player, String password) {
        if(this.password.equals(password) && player.getGame().equals(this)) {
            players.add((LocalPlayer)player);
            manager.sendEvent(new PlayerJoined(this, player));
            ((GameClient)getManager()).sendEvent(new PlayerJoined(this, player));
        } else {
            ((GameClient) getManager()).sendEvent(new FailedToJoin(this));
        }
    }

    @Override
    public void submit(String value) {
        if(me.getState().equals(PlayerState.Playing)) {
            //TODO: handle a submit
        }
    }
}

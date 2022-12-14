package yh.fabulousstars.hangman.client;

import yh.fabulousstars.hangman.client.events.CreateFailed;
import yh.fabulousstars.hangman.client.events.GameCreated;

import java.net.CookieManager;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

public class GameClient implements IGameManager {
    private final ArrayList<LocalGame> games;
    private final String backendUrl;
    private IGameEventHandler handler;
    private String clientId;
    private HttpClient http;


    public GameClient(String backendUrl, IGameEventHandler handler) {
        this.backendUrl = backendUrl;
        this.games = new ArrayList<>();
        this.handler = handler;
        this.clientId = null;
        this.http = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public void getGames() {


    }

    @Override
    public void createGame(String name, String playerName, String password) {

        for (var game : games) {
            if(game.getName().equals(name)) {
                sendEvent(new CreateFailed());
                return;
            }
        }
        var game = new LocalGame(this, name, playerName, clientId, password);
        games.add(game);

        sendEvent(new GameCreated(game, game.getMe()));
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    void sendEvent(IGameEvent event) {
        handler.handleGameEvent(event);
    }
}

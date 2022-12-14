package yh.fabulousstars.hangman.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import yh.fabulousstars.hangman.client.events.ClientConnect;
import yh.fabulousstars.hangman.client.events.GameCreate;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameClient implements IGameManager {
    private static final long POLL_MS = 3000;
    private final ArrayList<LocalGame> games;
    private final String backendUrl;
    private IGameEventHandler handler;
    private String clientName;
    private String clientPassword;
    private LocalPlayer player;
    private final HttpClient http;
    protected final Gson gson;
    private final Thread thread;

    /**
     * Construct a game client.
     * @param backendUrl api
     * @param handler game event handler
     */
    public GameClient(String backendUrl, IGameEventHandler handler) {
        this.backendUrl = backendUrl;
        this.games = new ArrayList<>();
        this.handler = handler;
        this.clientName = null;
        this.player = null;
        this.http = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.gson = new GsonBuilder()
                .serializeNulls()
                .create();
        this.thread = new Thread(() -> clientThread());
    }

    private void clientThread() {
        long nextPoll = 0; // next poll time
        while(true) {
            // poll delay
            var delay = nextPoll - System.currentTimeMillis();
            if (delay < 0) {
                // poll
                nextPoll = System.currentTimeMillis() + POLL_MS;
                threadPoll();
            } else {
                // sleep
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
            }
        }
    }

    private <T> T fromJson(String json) {
        var typeToken = new TypeToken<T>() {}.getType();
        return gson.fromJson(json, typeToken);
    }

    private void threadPoll() {
        var pollType = new TypeToken<HashMap<String,String>>() {}.getType();
        try {
            var req = HttpRequest.newBuilder(new URI(backendUrl + "/api/poll"))
                .GET().build();
            var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            HashMap<String,String> event = fromJson(resp.body());
            if(event != null) {
                sendEvent(event);
            }
        } catch (Exception e) { }
    }

    /**
     * Send GET or PUT request to server depending on if there is a body or not.
     * @param method Api method.
     * @param body Body to send as json
     */
    private void request(String method, Object body) {
        try {
            var url = backendUrl + "/api/"+method;
            var builder = HttpRequest.newBuilder(new URI(url));
            if(body != null) {
                builder.PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)));
            } else {
                builder.GET();
            }
            http.send(builder.build(), HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) { }
    }

    @Override
    public void listGames() {
        request(String.format("list?name=%s&pass=%s", clientName, clientPassword),null);
    }

    @Override
    public void createGame(String name, String playerName, String password) {
        request(String.format("create?name=%s&password=%s", clientName, clientPassword),null);
    }

    @Override
    public IPlayer getClient() {
        return player;
    }

    @Override
    public void connect(String name, String password) {
        // url encode
        clientName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        clientPassword =  URLEncoder.encode(password, StandardCharsets.UTF_8);
        // make connect request
        request(String.format("connect?name=%s", clientName),null);
        // start polling
        thread.start();
    }

    public void join(String gameId) {
        if(player != null) {
            request(String.format("join?game=%s", gameId),null);
        }
    }

    public void submit(String client, String value) {
    }

    /**
     * Send event on ui thread.
     *
     *  created: Game
     *  connected: Player
     *  connect_error: Message
     *  created
     *  joined: Game
     *  started: Game
     *
     * @param serverEvent
     */
    void sendEvent(Map<String,String> serverEvent) {
        IGameEvent gameEvent = switch (serverEvent.get("type")) {
            case "connected", "connect_error" -> getClientConnect(serverEvent);
            case "created", "create_error" -> getGameCreate(serverEvent);
            default -> null;
        };
        if(gameEvent != null) {
            Platform.runLater(() -> handler.handleGameEvent(gameEvent));
        }
    }

    /**
     * Build a GameCreate event from serverEvent.
     *
     * @param serverEvent
     * @return GameCreate
     */
    private IGameEvent getGameCreate(Map<String, String> serverEvent) {
        if(serverEvent.containsKey("error")) {
            return new GameCreate(null, serverEvent.get("error"));
        } else {
            var game = new LocalGame(this,
                    serverEvent.get("gameId"),
                    serverEvent.get("name"));
            games.add(game);
            return new GameCreate(game, null);
        }
    }

    /**
     * Build a ClientConnect event from serverEvent.
     *
     * @param serverEvent
     * @return ClientConnect
     */
    private IGameEvent getClientConnect(Map<String, String> serverEvent) {
        if(serverEvent.containsKey("error")) {
            return new ClientConnect(null, serverEvent.get("error"));
        }
        else {
            var id = serverEvent.get("clientId");
            var name = serverEvent.get("name");
            player = new LocalPlayer(name, id);
            return new ClientConnect(player,null);
        }
    }
}

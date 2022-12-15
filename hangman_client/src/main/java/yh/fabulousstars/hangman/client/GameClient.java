package yh.fabulousstars.hangman.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import yh.fabulousstars.hangman.client.events.*;

import java.lang.reflect.Type;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameClient implements IGameManager {
    private static final long POLL_MS = 3000;
    private LocalGame currentGames;
    private final String backendUrl;
    private IGameEventHandler handler;
    private String clientName;
    private String clientPassword;
    private LocalPlayer player;
    private final HttpClient http;
    protected final Gson gson;
    private final Thread thread;
    private boolean abort = false;

    /**
     * Construct a game client.
     * @param backendUrl api
     * @param handler game event handler
     */
    public GameClient(String backendUrl, IGameEventHandler handler) {
        this.backendUrl = backendUrl;
        this.currentGames = null;
        this.handler = handler;
        this.clientName = null;
        this.player = null;
        var cookieman = new CookieManager();
        cookieman.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        this.http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(cookieman)
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.gson = new GsonBuilder()
                .serializeNulls()
                .create();
        this.thread = new Thread(() -> clientThread());
    }

    private void clientThread() {
        long nextPoll = 0; // next poll time
        while(!abort) {
            // poll delay
            if (nextPoll < System.currentTimeMillis()) {
                // poll
                if(threadPoll()) {
                    nextPoll = System.currentTimeMillis() + POLL_MS;
                    continue;
                }
            }
            // sleep
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {}
        }
    }

    private <T> T fromJson(String json) {
        return gson.fromJson(json, new TypeToken<T>() {});
    }

    /**
     * Poll for game event.
     * @return true for delay
     */
    private boolean threadPoll() {
        try {
            var req = HttpRequest.newBuilder(new URI(backendUrl + "/api/poll"))
                .GET().build();
            var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            Map<String,String> event = fromJson(resp.body());
            if(event != null) {
                sendEvent(event);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listGames() {
        request("list",null);
    }

    @Override
    public void createGame(String name, String password) {
        var gameName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        var gamePassword =  URLEncoder.encode(password, StandardCharsets.UTF_8);
        request(String.format("create?name=%s&password=%s", gameName, gamePassword),null);
    }

    @Override
    public IPlayer getClient() {
        return player;
    }

    @Override
    public void connect(String name) {
        // url encode
        clientName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        // make connect request
        request(String.format("connect?name=%s", clientName),null);
        // start polling
        if(!thread.isAlive()) {
            abort = false;
            thread.start();
        }
    }

    @Override
    public void disconnect() {
        request("disconnect",null);
        abort = true;
    }

    /**
     * Join game.
     * @param gameId
     */
    public void join(String gameId, String password) {
        if(player != null) {
            if(password==null) {
                password = "";
            }
            request(String.format("join?game=%s&pass=%s", gameId, password),null);
        }
    }

    /**
     * Submit input to game.
     * @param client
     * @param value
     */
    public void submit(String client, String value) {
        //TODO: submit
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
            case "game_list" -> getGameList(serverEvent);
            case "player_list" -> getPlayerList(serverEvent);
            case "join" -> getJoinGame(serverEvent);
            default -> null;
        };
        if(gameEvent != null) {
            Platform.runLater(() -> handler.handleGameEvent(gameEvent));
        }
    }

    /**
     * Build a JoinGame event from serverEvent.
     * @param serverEvent
     * @return
     */
    private IGameEvent getJoinGame(Map<String, String> serverEvent) {
        if(serverEvent.containsKey("error")) {
            return new JoinGame(null, serverEvent.get("error"));
        } else {
            Map<String,String> gameMap = fromJson(serverEvent.get("json"));
            currentGames = new LocalGame(this,
                    gameMap.get("gameId"),
                    gameMap.get("name"));
            return new JoinGame(currentGames, null);
        }
    }

    /**
     * Build a PlayerList event from serverEvent.
     * @param serverEvent
     * @return
     */
    private IGameEvent getPlayerList(Map<String, String> serverEvent) {
        List<Map<String,String>> mapList = fromJson(serverEvent.get("json"));
        List<PlayerList.Player> players = new ArrayList<>();
        for (var map : mapList) {
            players.add(new PlayerList.Player(
                    map.get("clientId"),
                    map.get("name")
            ));
        }
        return new PlayerList(players);
    }

    /**
     * Build a GameList event from serverEvent.
     * @param serverEvent
     * @return
     */
    private IGameEvent getGameList(Map<String, String> serverEvent) {
        List<Map<String,String>> mapList = fromJson(serverEvent.get("json"));
        List<GameList.Game> games = new ArrayList<>();
        for (var map : mapList) {
            games.add(new GameList.Game(
                    map.get("gameId"),
                    map.get("name")
            ));
        }
        return new GameList(games);
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
            currentGames = new LocalGame(this,
                    serverEvent.get("gameId"),
                    serverEvent.get("name"));
            return new GameCreate(currentGames, null);
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

    public void shutdown() {
        try {
            abort = true;
            if(thread.isAlive()) {
                thread.join();
            }
        } catch (InterruptedException e) {
        }
    }

    //TODO: Remove
    public IGame getDummyGame() {
        return new LocalGame(this, "dummy-id", "Some game");
    }
}

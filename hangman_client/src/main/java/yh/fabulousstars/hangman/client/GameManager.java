package yh.fabulousstars.hangman.client;

import javafx.application.Platform;
import yh.fabulousstars.hangman.client.events.*;
import yh.fabulousstars.hangman.game.EventObject;
import yh.fabulousstars.hangman.game.GameInfo;
import yh.fabulousstars.hangman.game.PlayerInfo;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class GameManager implements IGameManager {
    private static final long POLL_MS = 3000;
    private final String backendUrl;
    private final HttpClient http;
    private final Thread thread;
    private LocalGame currentGame;
    private IGameEventHandler handler;
    private String clientName;
    private LocalPlayer player;
    private boolean abort = false;

    /**
     * Construct a game client.
     *
     * @param backendUrl api
     * @param handler    game event handler
     */
    GameManager(String backendUrl, IGameEventHandler handler) {
        this.backendUrl = backendUrl;
        this.currentGame = null;
        this.handler = handler;
        this.clientName = null;
        this.player = null;
        var cookieMan = new CookieManager();
        cookieMan.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        this.http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(cookieMan)
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.thread = new Thread(() -> clientThread());
    }

    private void clientThread() {
        long nextPoll = 0; // next poll time
        while (!abort) {
            // poll delay
            if (nextPoll < System.currentTimeMillis()) {
                // poll
                if (threadPoll()) {
                    nextPoll = System.currentTimeMillis() + POLL_MS;
                    continue;
                }
            }
            // sleep
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Poll for game event.
     *
     * @return true for delay
     */
    private boolean threadPoll() {
        try {
            var req = HttpRequest.newBuilder(new URI(backendUrl + "/api/poll"))
                    .GET().build();
            var resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() >= 400) {
                sendEvent(new EventObject("reset"));
                return true;
            }
            var event = ObjectHelper.fromBytes(resp.body());
            if (event != null) {
                if (!((EventObject) event).getName().equals("idle")) {
                    sendEvent((EventObject) event);
                    return false;
                }
            }
        } catch (ConnectException ex) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Send GET or PUT request to server depending on if there is a body or not.
     *
     * @param method Api method.
     */
    private void request(String method) {
        try {
            var url = backendUrl + "/api/" + method;
            var req = HttpRequest.newBuilder(new URI(url))
                    .GET().build();
            http.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listGames() {
        request("list");
    }

    @Override
    public IGame getGame() {
        return currentGame;
    }

    @Override
    public void createGame(String name, String password) {
        var gameName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        var gamePassword = URLEncoder.encode(password, StandardCharsets.UTF_8);
        request(String.format("create?name=%s&password=%s", gameName, gamePassword));
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
        request(String.format("connect?name=%s", clientName));
        // start polling
        if (!thread.isAlive()) {
            abort = false;
            thread.start();
        }
    }

    @Override
    public void disconnect() {
        request("disconnect");
        currentGame = null;
        player = null;
        abort = true;
    }

    /**
     * Join game.
     *
     * @param gameId
     */
    public void join(String gameId, String password) {
        if (player != null) {
            if (password == null) {
                password = "";
            }
            request(String.format("join?game=%s&pass=%s", gameId, password));
        }
    }

    void start() {
        if (currentGame != null) {
            request(String.format("start?game=%s", currentGame.getId()));
        }
    }

    /**
     * Leave game.
     */
    void leave() {
        request("leave");
    }

    /**
     * Submit a word.
     *
     * @param value
     */
    public void submitWord(String value) {
        request(String.format("word?str=%s", value));
    }

    /**
     * Submit a guess.
     *
     * @param value
     */
    public void submitGuess(String value) {
        request(String.format("guess?str=%s", value));
    }

    /**
     * Send event on ui thread.
     * <p>
     * created: Game
     * connected: Player
     * connect_error: Message
     * created
     * joined: Game
     * started: Game
     *
     * @param serverEvent
     */
    void sendEvent(EventObject serverEvent) {
        IGameEvent gameEvent = switch (serverEvent.getName()) {
            case "connected", "connect_error" -> getClientConnect(serverEvent);
            case "created", "create_error" -> getCreateOrJoin(serverEvent);
            case "game_list" -> getGameList(serverEvent);
            case "player_list" -> getPlayerList(serverEvent);
            case "join", "join_error" -> getCreateOrJoin(serverEvent);
            case "leave" -> getLeaveGame(serverEvent);
            case "message" -> getChatMessage(serverEvent);
            case "play_state" -> getPlayerState(serverEvent);
            case "submit_guess" -> getSubmitGuess(serverEvent);
            case "request_word" -> getRequestWord(serverEvent);
            case "request_guess" -> getRequestGuess(serverEvent);
            case "reset" -> new ResetClient();
            default -> null;
        };
        if (gameEvent != null) {
            Platform.runLater(() -> handler.handleGameEvent(gameEvent));
        }
    }

    /**
     * Build a SubmitGuess event from serverEvent.
     *
     * @param serverEvent
     * @return SubmitGuess
     */
    private IGameEvent getSubmitGuess(EventObject serverEvent) {
        return new SubmitGuess(
                serverEvent.get("correct").equals("1"),
                serverEvent.get("guess"));
    }

    /**
     * Build a RequestWord event from serverEvent.
     *
     * @param serverEvent
     * @return RequestWord
     */
    private IGameEvent getRequestWord(EventObject serverEvent) {
        return new RequestWord(
                Integer.parseInt(serverEvent.get("minLength")),
                Integer.parseInt(serverEvent.get("maxLength"))
        );
    }

    /**
     * Build a RequestGuess event from serverEvent.
     *
     * @param serverEvent
     * @return RequestGuess
     */
    private IGameEvent getRequestGuess(EventObject serverEvent) {
        return new RequestGuess();
    }

    /**
     * Build a PlayerState event from serverEvent.
     *
     * @param serverEvent
     * @return PlayerState
     */
    private IGameEvent getPlayerState(EventObject serverEvent) {
        PlayState playState = (PlayState) serverEvent.getPayload();
        var player = (LocalPlayer) currentGame.getPlayer(playState.getClientId());
        player.setPlayState(playState);
        return new PlayerState(playState.getClientId(), playState);
    }

    /**
     * Build a ChatMessage event from serverEvent.
     *
     * @param serverEvent
     * @return ChatMessage
     */
    private IGameEvent getChatMessage(EventObject serverEvent) {
        var inGame = serverEvent.get("inGame").equals("1");
        var message = serverEvent.get("message");
        return new ChatMessage(message, inGame);
    }

    /**
     * Build a LeaveGame event from serverEvent.
     *
     * @param serverEvent
     * @return LeaveGame
     */
    private IGameEvent getLeaveGame(EventObject serverEvent) {
        var gameId = serverEvent.get("gameId");
        currentGame = null;
        return new LeaveGame(gameId);
    }

    /**
     * Build a JoinGame event from serverEvent.
     *
     * @param serverEvent
     * @return JoinGame
     */
    private IGameEvent getCreateOrJoin(EventObject serverEvent) {
        if (serverEvent.contains("error")) {
            return new JoinOrCreate(null, serverEvent.get("error"));
        } else {
            currentGame = new LocalGame(this,
                    serverEvent.get("gameId"),
                    serverEvent.get("name"));
            player.setGame(currentGame);
            return new JoinOrCreate(currentGame, null);
        }
    }

    /**
     * Build a PlayerList event from serverEvent.
     *
     * @param serverEvent
     * @return PlayerList
     */
    private IGameEvent getPlayerList(EventObject serverEvent) {
        List<PlayerInfo> infList = (List<PlayerInfo>) serverEvent.getPayload();
        var gameId = serverEvent.get("gameId");
        List<IPlayer> players = new ArrayList<>();
        if (currentGame != null && !currentGame.getId().equals(gameId)) {
            // getting player list when not in game should never happen
            throw new RuntimeException("Incorrect game id!");
        }
        for (var playerInf : infList) {
            var player = new LocalPlayer(
                    currentGame,
                    playerInf.getName(),
                    playerInf.getClientId()
            );
            players.add(player);
        }
        return new PlayerList(players, gameId != null);
    }

    /**
     * Build a GameList event from serverEvent.
     *
     * @param serverEvent
     * @return GameList
     */
    private IGameEvent getGameList(EventObject serverEvent) {
        return new GameList((List<GameInfo>) serverEvent.getPayload());
    }

    /**
     * Build a ClientConnect event from serverEvent.
     *
     * @param serverEvent
     * @return ClientConnect
     */
    private IGameEvent getClientConnect(EventObject serverEvent) {
        if (serverEvent.contains("error")) {
            return new ClientConnect(null, serverEvent.get("error"));
        } else {
            var id = serverEvent.get("clientId");
            var name = serverEvent.get("name");
            player = new LocalPlayer(null, name, id);
            return new ClientConnect(player, null);
        }
    }

    public void shutdown() {
        try {
            abort = true;
            if (thread.isAlive()) {
                thread.join();
            }
        } catch (InterruptedException e) {
        }
    }
}

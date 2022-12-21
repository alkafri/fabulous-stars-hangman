package yh.fabulousstars.hangman.client;

import javafx.application.Platform;
import yh.fabulousstars.hangman.client.events.*;
import yh.fabulousstars.hangman.game.*;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

class GameManager implements IGameManager {
    private static final long POLL_MS = 1500;
    private final String backendUrl;
    private final HttpClient http;
    private final Thread thread;
    private LocalGame currentGame;
    private IGameEventHandler handler;
    private String clientName;
    private LocalPlayer thisPlayer;
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
        this.thisPlayer = null;
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
                sendEvent(new GameEvent(GameEventType.Reset));
                return true;
            }
            var event = ObjectHelper.fromBytes(resp.body());
            if (event != null) {
                if (!((GameEvent) event).getType().equals(GameEventType.Idle)) {
                    sendEvent((GameEvent) event);
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
    private boolean request(String method) {
        try {
            var url = backendUrl + "/api/" + method;
            var req = HttpRequest.newBuilder(new URI(url))
                    .GET().build();
            http.send(req, HttpResponse.BodyHandlers.discarding());

            return true;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return false;
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
        request(String.format("create?name=%s&password=%s", enc(name), enc(password)));
    }

    @Override
    public IPlayer getClient() {
        return thisPlayer;
    }

    @Override
    public void connect(String name) {
        // url encode
        clientName = name;
        // make connect request
        if (request(String.format("connect?name=%s", enc(clientName)))) {
            // start polling
            if (!thread.isAlive()) {
                abort = false;
                thread.start();
            }
        } else {
            Platform.runLater(() -> {
                var event = new GameEvent(GameEventType.Connect_error);
                event.put("error", "Connection error.");
                sendEvent(event);
            });
        }
    }

    @Override
    public void disconnect() {
        if (thisPlayer != null) {
            request("disconnect");
            currentGame = null;
            thisPlayer = null;
        }
        abort = true;
    }

    /**
     * Join game.
     *
     * @param gameId
     */
    public void join(String gameId, String password) {
        if (thisPlayer != null) {
            if (password == null) {
                password = "";
            }
            request(String.format("join?game=%s&pass=%s", gameId, enc(password)));
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
    @Override
    public void submitWord(String value) {
        request(String.format("word?str=%s", enc(value)));
    }

    /**
     * Submit a guess.
     *
     * @param value
     */
    @Override
    public void submitGuess(String value) {
        request(String.format("guess?str=%s", enc(value)));
    }

    @Override
    public void say(String message) {
        message = message.strip();
        if (!message.isEmpty()) {
            request(String.format("say?str=%s", enc(message)));
        }
    }

    private String enc(String value) {
        return URLEncoder.encode(
                value,
                Charset.defaultCharset()
        );
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
    void sendEvent(GameEvent serverEvent) {
        IGameEvent gameEvent = switch (serverEvent.getType()) {
            case Connected,
                    Connect_error -> getClientConnect(serverEvent);
            case Created,
                    Create_error,
                    Join,
                    Join_error -> getCreateOrJoin(serverEvent);
            case Game_list -> getGameList(serverEvent);
            case Player_list -> getPlayerList(serverEvent);
            case Leave -> getLeaveGame(serverEvent);
            case Message -> getChatMessage(serverEvent);
            case Game_started -> getGameStarted(serverEvent);
            case Play_state -> getPlayState(serverEvent);
            case Guess_result -> getGuessResult(serverEvent);
            case Request_word -> getRequestWord(serverEvent);
            case Request_guess -> getRequestGuess(serverEvent);
            case Winner, Loser -> getGameOver(serverEvent);
            case Reset -> new ResetClient();
            default -> null;
        };
        if (gameEvent != null) {
            Platform.runLater(() -> handler.handleGameEvent(gameEvent));
        }
    }

    private IGameEvent getGameOver(GameEvent serverEvent) {
        currentGame.leave();
        thisPlayer.setGame(null);
        return new GameOver(serverEvent.getType().equals(GameEventType.Winner));
    }

    private IGameEvent getGameStarted(GameEvent serverEvent) {
        return new GameStarted();
    }

    /**
     * Build a SubmitGuess event from serverEvent.
     *
     * @param serverEvent
     * @return SubmitGuess
     */
    private IGameEvent getGuessResult(GameEvent serverEvent) {
        return new GuessResult(
                serverEvent.get("correct").equals("1"),
                serverEvent.get("finished").equals("1"),
                serverEvent.get("guess"));
    }

    /**
     * Build a RequestWord event from serverEvent.
     *
     * @param serverEvent
     * @return RequestWord
     */
    private IGameEvent getRequestWord(GameEvent serverEvent) {
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
    private IGameEvent getRequestGuess(GameEvent serverEvent) {
        return new RequestGuess();
    }

    /**
     * Build a PlayState event from serverEvent.
     *
     * @param serverEvent
     * @return PlayState
     */
    private IGameEvent getPlayState(GameEvent serverEvent) {
        List<PlayState> playStates = (List<PlayState>) serverEvent.getPayload();
        // update state for local player
        for (var player : currentGame.getPlayers()) {
            for (var state : playStates) {
                if (player.getClientId().equals(state.getClientId())) {
                    ((LocalPlayer) player).setPlayState(state);
                    break;
                }
            }
        }
        return new PlayerState(currentGame.getPlayers());
    }

    /**
     * Build a ChatMessage event from serverEvent.
     *
     * @param serverEvent
     * @return ChatMessage
     */
    private IGameEvent getChatMessage(GameEvent serverEvent) {
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
    private IGameEvent getLeaveGame(GameEvent serverEvent) {
        var gameId = serverEvent.get("gameId");
        if (serverEvent.get("clientId").equals(thisPlayer.getClientId())) {
            currentGame = null;
            thisPlayer.setGame(null);
            thisPlayer.setPlayState(null);
        }
        return new LeaveGame(gameId);
    }

    /**
     * Build a JoinGame event from serverEvent.
     *
     * @param serverEvent
     * @return JoinGame
     */
    private IGameEvent getCreateOrJoin(GameEvent serverEvent) {
        if (serverEvent.contains("error")) {
            return new JoinOrCreate(null, serverEvent.get("error"));
        } else {
            currentGame = new LocalGame(this,
                    serverEvent.get("gameId"),
                    serverEvent.get("name"));
            thisPlayer.setGame(currentGame);
            return new JoinOrCreate(currentGame, null);
        }
    }

    /**
     * Build a PlayerList event from serverEvent.
     *
     * @param serverEvent
     * @return PlayerList
     */
    private IGameEvent getPlayerList(GameEvent serverEvent) {
        List<PlayerInfo> infList = (List<PlayerInfo>) serverEvent.getPayload();
        var gameId = serverEvent.get("gameId");
        List<IPlayer> players = new ArrayList<>();
        if (currentGame != null && !currentGame.getId().equals(gameId)) {
            // getting player list when not in game should never happen
            throw new RuntimeException("Incorrect game id!");
        }
        for (var playerInf : infList) {
            IPlayer player = currentGame == null ? null : currentGame.getPlayer(playerInf.getClientId());
            if (player == null) {
                player = new LocalPlayer(
                        this,
                        currentGame,
                        playerInf.getName(),
                        playerInf.getClientId()
                );
                if (currentGame != null) {
                    currentGame.addPlayer((LocalPlayer) player);
                }
            }
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
    private IGameEvent getGameList(GameEvent serverEvent) {
        return new GameList((List<GameInfo>) serverEvent.getPayload());
    }

    /**
     * Build a ClientConnect event from serverEvent.
     *
     * @param serverEvent
     * @return ClientConnect
     */
    private IGameEvent getClientConnect(GameEvent serverEvent) {
        if (serverEvent.contains("error")) {
            return new ClientConnect(null, serverEvent.get("error"));
        } else {
            var id = serverEvent.get("clientId");
            var name = serverEvent.get("name");
            thisPlayer = new LocalPlayer(this, null, name, id);
            return new ClientConnect(thisPlayer, null);
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

package yh.fabulousstars.hangman.server;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import yh.fabulousstars.hangman.game.EventObject;
import yh.fabulousstars.hangman.game.*;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.*;

/**
 * Game servlet handles game requests.
 */
@WebServlet(name = "GameServlet", value = "/api/*")
public class GameServlet extends BaseServlet {
    private static final int MAX_PLAYERS_PER_GAME = 6;

    public GameServlet() {
        super();
    }

    @Override
    protected void handleRequest(RequestContext ctx) throws IOException {
        // respond to the declared requests
        switch (ctx.endpoint()) {
            case "poll" -> poll(ctx);
            case "word" -> gameWord(ctx);
            case "guess" -> gameGuess(ctx);
            case "say" -> say(ctx);
            case "connect" -> lobbyConnect(ctx, true);
            case "disconnect" -> lobbyConnect(ctx, false);
            case "leave" -> gameLeave(ctx);
            case "join" -> lobbyJoin(ctx);
            case "create" -> lobbyCreate(ctx);
            case "message" -> message(ctx);
            case "listgames" -> listGames(ctx, false);
            case "listplayers" -> listPlayers(ctx, false);
            case "start" -> startGame(ctx);
        }
    }

    private void say(RequestContext ctx) {
        var message= ctx.req().getParameter("str");
        var players = getPlayerEntities(ctx);
        var entity = getEntity(PLAYER_TYPE, ctx.session());
        var inGame = entity.getProperty("gameId")==null ? "0" : "1";
        var event = new EventObject("message",
                Map.of("message", message,
                        "inGame", inGame)
        );
        for(var player : players) {
            addEvent(player.getKey().getName(),event);
        }
    }

    /**
     * Request game start.
     * @param ctx
     */
    private void startGame(RequestContext ctx) {
        var gameId = ctx.req().getParameter("game"); // id
        if (gameId != null) {
            // get state object
            var gameState = getGameState(gameId);
            if (gameState != null) {
                // set started
                gameState.setStarted(true);
                // initial events
                var startedEvent = new EventObject("game_started");
                var wordEvent = new EventObject("request_word");
                // players
                var players = gameState.getPlayerEntries();
                // send events to participants
                for (var player : players) {
                    var target = player.getKey();
                    addEvent(target, startedEvent);
                    addEvent(target, wordEvent);
                }
            }
        }
    }

    /**
     * Leave game and refresh player list for participants.
     * <p>
     * events: list_players, leave
     *
     * @param ctx
     */
    private void gameLeave(RequestContext ctx) {
        // get player
        var player = getEntity(PLAYER_TYPE, ctx.session());
        if (player == null) {
            return;
        }
        // get game id
        var gameId = player.getProperty("gameId").toString();
        if (gameId == null) {
            return;
        }
        // clear game id from player
        player.setProperty("gameId", null);
        datastore.put(player);
        // update player list
        listGamePlayers(ctx, gameId, "list_players");

        var event = new EventObject("leave");
        event.put("gameId", gameId);
        addEvent(ctx.session(), event);
    }

    private void listGamePlayers(RequestContext ctx, String gameId, String eventName) {
        // get players in game
        var query = new Query(PLAYER_TYPE)
                .setKeysOnly()
                .setFilter(new Query.FilterPredicate("gameId", Query.FilterOperator.EQUAL, gameId));
        var entities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        var players = new ArrayList<PlayerInfo>();
        for(var entity : entities) {
            players.add(new PlayerInfo(
                    (String)entity.getProperty("name"),
                    entity.getKey().getName(),
                    gameId
            ));
        }
        // create event containing players
        var event = new EventObject(eventName);
        event.put("gameId", gameId);
        event.setPayload(players);
        // broadcast event to participants
        for (var player : players) {
            addEvent(player.getClientId(), event);
        }
    }

    /**
     * Client connect.
     * <p>
     * events: connected, connect_error
     *
     * @param ctx
     * @param connect
     * @throws IOException
     */
    private void lobbyConnect(RequestContext ctx, boolean connect) throws IOException {
        var clientId = ctx.session();

        // connection or disconnection?
        if (connect) {
            // connection request. check player name
            var name = checkName(ctx.req().getParameter("name"), PLAYER_TYPE);
            if (name != null) {
                var entity = new Entity(PLAYER_TYPE, clientId);
                var event = new EventObject("connected");
                entity.setProperty("name", name);
                entity.setProperty("gameId", null);
                event.put("name", name);
                event.put("gameId", null);
                // store in db
                setExpiry(entity);
                datastore.put(entity);
                addEvent(clientId, event);
                listPlayers(ctx, true);
                listGames(ctx, false);
            } else {
                var event = new EventObject("connect_error");
                event.put("error", "Name error.");
                // name not ok. empty response
                addEvent(clientId, event);
            }
        } else {
            // disconnect
            deleteClient(clientId);
            listPlayers(ctx, true);
        }
    }

    /**
     * Create game from lobby and join it.
     * <p>
     * events: "created", "create_error"
     *
     * @param ctx
     */
    private void lobbyCreate(RequestContext ctx) {
        // get player
        Entity player = getEntity(PLAYER_TYPE, ctx.session());
        if (player == null) {
            return;
        }

        // if not in game...
        if (player.getProperty("gameId") == null) {
            // name and password
            var name = checkName(ctx.req().getParameter("name"), GAME_TYPE);
            var pass = ctx.req().getParameter("password");
            if (name != null) { //if name
                if (pass != null) { // if provided pass, adjust it
                    pass = pass.trim();
                    if (pass.equals("")) pass = null;
                }
                // create game meta
                // use client id as game id. This will also let us determine owner
                var entity = new Entity(GAME_TYPE, ctx.session());
                entity.setProperty("name", name);
                entity.setProperty("password", pass);
                entity.setProperty("owner", ctx.session());
                var key = datastore.put(entity); // game id from db
                // create game state
                var state = new GameState();
                state.addPlayer(ctx.session());
                putGameState(key.getName(), state);
                // update player game
                player.setProperty("gameId", key.getName());
                datastore.put(player);
                // get game meta entity as map
                var event = new EventObject("created");
                event.put("gameId", key.getName());
                event.put("name", name);
                event.put("owner", ctx.session());
                addEvent(ctx.session(), event); // send game created to player
                listGames(ctx, true); // broadcast list of games
                return;
            }
        }
        // fail
        addEvent(ctx.session(), new EventObject(
                "create_error",
                Map.of("error", "Error creating game."))
        );
    }

    /**
     * Join a game.
     * <p>
     * events: join, game_list
     *
     * @param ctx
     */
    private void lobbyJoin(RequestContext ctx) {
        // get session
        var clientId = ctx.session();
        // get parameters
        var gameId = ctx.req().getParameter("game");
        var password = ctx.req().getParameter("pass");
        if (gameId != null) {
            if (password == null) {
                password = "";
            }
            try {
                gameId = gameId.trim();
                // get game from db
                var gameEntity = datastore.get(KeyFactory.createKey(GAME_TYPE, gameId));
                // if db pass exists and user pass is correct
                var pw = (String)gameEntity.getProperty("password");
                if (pw == null || password.equals(pw)) {
                    // check if game is full
                    var query = new Query(PLAYER_TYPE)
                            .setFilter(new Query.FilterPredicate("gameId", Query.FilterOperator.EQUAL, gameId))
                            .setKeysOnly();
                    var currentPlayerCount = datastore.prepare(query).countEntities(FetchOptions.Builder.withDefaults());
                    if (currentPlayerCount >= MAX_PLAYERS_PER_GAME) {
                        addEvent(clientId, new EventObject("join_error", Map.of(
                                "error", "Game is full."
                        )));
                        return;
                    }
                    // update player
                    var playerEntity = datastore.get(KeyFactory.createKey(PLAYER_TYPE, clientId));
                    playerEntity.setProperty("gameId", gameId);
                    datastore.put(playerEntity);
                    // send join event
                    var event = new EventObject("join", getStringProperties(gameEntity));
                    event.put("gameId", gameId);
                    addEvent(clientId, event);
                    listGamePlayers(ctx, gameId, "player_list");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // send failed join event
        addEvent(clientId, new EventObject("join_error", Map.of(
                "error", "Join failed."
        )));
    }

    /**
     * Make a guess.
     *
     * @param ctx
     */
    private void gameGuess(RequestContext ctx) {
        var clientId = ctx.session();
        var guess = ctx.req().getParameter("guess");
        var playerEntity = getEntity(PLAYER_TYPE, clientId);
        if (playerEntity != null) {
            var gameId = playerEntity.getProperty("gameId").toString();
            var gameState = getGameState(gameId); // get state
            var playerStates = GameLogics.makeGuess(gameState, clientId, guess); // play
            putGameState(gameId, gameState); // store state
            // send changed states
            var event = new EventObject("play_state");
            for (var state : playerStates) {
                event.setPayload(state);
                addEvent(state.getClientId(), event);
            }
        }
    }

    /**
     * Set player word.
     *
     * @param ctx
     */
    private void gameWord(RequestContext ctx) {
        var clientId = ctx.session();
        var word = ctx.req().getParameter("word");
        var playerEntity = getEntity(PLAYER_TYPE, clientId);
        if (playerEntity != null) {
            var gameId = playerEntity.getProperty("gameId").toString();
            var gameState = getGameState(gameId); // get state
            var playerStates = GameLogics.setWord(gameState, clientId, word);
            putGameState(gameId, gameState); // store state
            var event = new EventObject("request_guess");
            for (var state : playerStates) {
                event.setPayload(state);
                addEvent(state.getClientId(), event);
            }
        }
    }

    /**
     * @param ctx
     */
    private void message(RequestContext ctx) {
        var clientId = ctx.session();
        // get params
        var gameId = ctx.req().getParameter("gameId");
        var message = ctx.req().getParameter("message");
        // find participant ids
        var query = new Query(PLAYER_TYPE)
                .setFilter(new Query.FilterPredicate("gameId", Query.FilterOperator.EQUAL, gameId))
                .setKeysOnly();
        var participants = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        var event = new EventObject("message");
        event.put("inGame", gameId == null ? "0" : "1");
        for (var participant : participants) {
            event.put("message", message);
            addEvent(participant.getKey().getName(), event);
        }
    }

    /**
     * Create a list_games event.
     * <p>
     * events: game_list
     *
     * @param ctx
     */
    private void listGames(RequestContext ctx, boolean broadcast) {
        // results
        var games = new ArrayList<GameInfo>();
        // iterate database games
        var iter = datastore.prepare(new Query(GAME_TYPE)).asIterator();
        while (iter.hasNext()) {
            var entity = iter.next(); // get datastore entity
            var pass = (String) entity.getProperty("password");
            games.add(new GameInfo(
                    entity.getKey().getName(),
                    (String) entity.getProperty("name"),
                    pass != null
            ));
        }
        var event = new EventObject("game_list");
        event.setPayload(games);
        if (broadcast) {
            // broadcast pollable event
            for (var id : getAllIds(PLAYER_TYPE)) {
                addEvent(id, event);
            }
        } else {
            // create pollable event for caller
            addEvent(ctx.session(), event);
        }
    }

    /**
     * List server players or just in-game players if client is in-game.
     *
     * @param ctx
     * @return list of players
     */
    private void listPlayers(RequestContext ctx, boolean broadcast) {
        var players = new ArrayList<PlayerInfo>();
        var entities = getPlayerEntities(ctx);
        for (var entity : entities) {
            var name = (String) entity.getProperty("name");
            var clientId = entity.getKey().getName();
            var gameId = (String) entity.getProperty("gameId");
            players.add(new PlayerInfo(name, clientId, gameId));
        }
        var event = new EventObject("player_list");
        var playerEntity = getEntity(PLAYER_TYPE, ctx.session());
        event.put("gameId", (String)playerEntity.getProperty("gameId"));
        event.setPayload(players);
        if (broadcast) {
            // broadcast pollable event
            for (var player : players) {
                addEvent(player.getClientId(), event);
            }
        } else {
            // create pollable event for caller
            addEvent(ctx.session(), event);
        }
    }

    private List<Entity> getPlayerEntities(RequestContext ctx) {
        var playerEntity = getEntity(PLAYER_TYPE, ctx.session());
        // get game id
        String currentGameId = (String)playerEntity.getProperty("gameId");
        var query = new Query(PLAYER_TYPE)
                .setFilter(new Query.FilterPredicate(
                    "gameId",
                        Query.FilterOperator.EQUAL,
                        currentGameId));
        return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    }

    /**
     * Check that name is at least 2 characters and doesn't exist.
     *
     * @param name name to check
     * @param type entity type to check
     * @return name trimmed name
     */
    private String checkName(String name, String type) {
        if (name != null) {
            name = name.trim();
            // at least 2 chars
            if (name.length() >= 2) {
                // check for existing
                var query = new Query(type)
                        .setFilter(new Query.FilterPredicate(
                                "name",
                                Query.FilterOperator.EQUAL,
                                name));
                if (datastore.prepare(query).asSingleEntity() == null) {
                    return name;
                }
            }
        }
        return null;
    }

    /**
     * Delete client/player
     *
     * @param clientId
     */
    private void deleteClient(String clientId) {
        // remove player
        datastore.delete(KeyFactory.createKey("Player", clientId));
        // remove remaining game events
        var query = new Query("GameEvent")
                .setKeysOnly()
                .setFilter(new Query.FilterPredicate(
                        "clientId", Query.FilterOperator.EQUAL, clientId));
        var keys = datastore.prepare(query)
                .asList(FetchOptions.Builder.withDefaults()).stream().map(ent -> ent.getKey());
        datastore.delete(keys.toList());
    }
}

package yh.fabulousstars.hangman.server;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import yh.fabulousstars.hangman.game.*;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Game servlet handles game requests.
 */
@WebServlet(name = "GameServlet", value = "/api/*")
public class GameServlet extends BaseServlet {
    public GameServlet() {
        super();
    }

    @Override
    protected void handleRequest(RequestContext ctx) throws IOException {
        // respond to requests in list
        switch (ctx.endpoint()) {
            case "poll" -> poll(ctx);
            case "word" -> setWord(ctx);
            case "guess" -> doGuess(ctx);
            case "say" -> say(ctx);
            case "connect" -> lobbyConnect(ctx, true);
            case "disconnect" -> lobbyConnect(ctx, false);
            case "leave" -> gameLeave(ctx);
            case "join" -> lobbyJoin(ctx);
            case "create" -> lobbyCreate(ctx);
            case "listgames" -> listGames(ctx, false);
            case "listplayers" -> listPlayers(ctx, false);
            case "start" -> startGame(ctx);
        }
    }

    /**
     * Handle say
     * Send message either to lobby or game room.
     * @param ctx
     */
    private void say(RequestContext ctx) {
        var message = ctx.req().getParameter("str");
        var players = getPlayerEntities(ctx);
        var entity = getEntity(PLAYER_TYPE, ctx.session());
        var inGame = entity.getProperty("gameId") == null ? "0" : "1";
        var event = new GameEvent(GameEventType.Message)
                .put("message", String.format("%s: %s", entity.getProperty("name"), message))
                .put("inGame", inGame);
        for (var player : players) {
            addEvent(player.getKey().getName(), event);
        }
    }

    /**
     * Request game start.
     *
     * @param ctx
     */
    private void startGame(RequestContext ctx) {
        var gameId = ctx.req().getParameter("game"); // id
        if (gameId != null) {
            // get state object
            var gameState = getGameState(gameId);
            if (gameState != null) {
                // set started
                var events = GameLogics.start(gameState);
                for (var event : events) {
                    addEvent(event.target(), event.event());
                }
            }
        }
    }


    /**
     * Leave game and refresh player list for participants.
     *
     * @param ctx
     */
    private void gameLeave(RequestContext ctx) {
        // get players
        var entities = getPlayerEntities(ctx);
        // player
        var clientId = ctx.session();
        Entity player = null;
        for (var entity : entities) {
            if (entity.getKey().getName().equals(clientId)) {
                player = entity;
                break;
            }
        }
        if (player == null) {
            return;
        }
        // get game id
        var gameId = (String) player.getProperty("gameId");
        // clear game id from player
        player.setProperty("gameId", null);
        datastore.put(player);
        var state = getGameState(gameId);
        state.removePlayer(clientId);
        putGameState(gameId, state);
        // update player list
        listGamePlayers(gameId);
        // to player
        var event = new GameEvent(GameEventType.Leave)
                .put("gameId", gameId)
                .put("clientId", ctx.session());
        addEvent(ctx.session(), event);
        listPlayers(ctx, true);
        // to remaining participants
        for (var entity : entities) {
            if (!entity.getKey().getName().equals(clientId)) {
                addEvent(ctx.session(), event);
            }
        }
    }

    /**
     * List players for game.
     * @param gameId
     */
    private void listGamePlayers(String gameId) {
        // get players in game
        var query = new Query(PLAYER_TYPE)
                .setKeysOnly()
                .setFilter(new Query.FilterPredicate("gameId", Query.FilterOperator.EQUAL, gameId));
        var entities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        var players = new ArrayList<PlayerInfo>();
        for (var entity : entities) {
            players.add(new PlayerInfo(
                    (String) entity.getProperty("name"),
                    entity.getKey().getName(),
                    gameId
            ));
        }
        // create event containing players
        var event = new GameEvent(GameEventType.Player_list)
                .put("gameId", gameId)
                .setPayload(players);
        // broadcast event to participants
        for (var player : players) {
            addEvent(player.getClientId(), event);
        }
    }

    /**
     * Client connection.
     *
     * @param ctx
     * @param connect connect or disconnect
     * @throws IOException
     */
    private void lobbyConnect(RequestContext ctx, boolean connect) throws IOException {
        var clientId = ctx.session();

        // connection or disconnection?
        if (connect) {
            // connection request.
            // check player name
            var name = checkName(ctx.req().getParameter("name"), PLAYER_TYPE);
            if (name != null) {
                // create player entity
                var entity = new Entity(PLAYER_TYPE, clientId);
                var event = new GameEvent(GameEventType.Connected)
                        .put("name", name)
                        .put("gameId", null)
                        .put("clientId", clientId);
                entity.setProperty("name", name);
                entity.setProperty("gameId", null);
                // store in db
                setExpiry(entity);
                datastore.put(entity);
                // send connect and list players/games event
                addEvent(clientId, event);
                listPlayers(ctx, true);
                listGames(ctx, false);
            } else {
                // error
                var event = new GameEvent(GameEventType.Connect_error)
                        .put("error", "Name error.");
                // name not ok. empty response
                addEvent(clientId, event);
            }
        } else {
            // disconnect
            try {
                deleteClient(clientId);
                listPlayers(ctx, true);
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Create game from lobby and join it.
     *
     * @param ctx
     */
    private void lobbyCreate(RequestContext ctx) {
        // get player
        var clientId = ctx.session();
        Entity player = getEntity(PLAYER_TYPE, clientId);
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
                var entity = new Entity(GAME_TYPE, clientId);
                entity.setProperty("name", name);
                entity.setProperty("password", pass);
                entity.setProperty("owner", clientId);
                datastore.put(entity); // game id from db
                // create game state
                var state = new GameState();
                state.addPlayer(ctx.session());
                putGameState(clientId, state);
                // update player
                player.setProperty("gameId", clientId);
                datastore.put(player);
                // created event
                var event = new GameEvent(GameEventType.Created)
                        .put("gameId", clientId)
                        .put("name", name)
                        .put("owner", ctx.session());
                addEvent(clientId, event); // send game created to player
                listGames(ctx, true); // broadcast list of games
                return;
            }
        }
        // fail
        addEvent(ctx.session(), new GameEvent(GameEventType.Create_error)
                .put("error", "Error creating game.")
        );
    }

    /**
     * Join a game.
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
            try {
                gameId = gameId.trim();
                // get game from db
                var gameEntity = getEntity(GAME_TYPE, gameId);
                // if db pass exists and user pass is correct
                var password2 = (String) gameEntity.getProperty("password");
                if (password2 == null || (password2.equals(password))) {
                    // check if game is full
                    var gameState = getGameState(gameId);
                    if (gameState.getPlayerStates().size() >= Config.MAX_PLAYERS_PER_GAME) {
                        addEvent(clientId, new GameEvent(GameEventType.Join_error)
                                .put("error", "Game is full.")
                        );
                        return;
                    }
                    // update player
                    gameState.addPlayer(clientId);
                    putGameState(gameId, gameState);
                    var playerEntity = getEntity(PLAYER_TYPE, clientId);
                    playerEntity.setProperty("gameId", gameId);
                    datastore.put(playerEntity);
                    // send join event
                    var event = new GameEvent(GameEventType.Join)
                            .put("name", (String) gameEntity.getProperty("name"))
                            .put("gameId", gameId);
                    addEvent(clientId, event);
                    listPlayers(ctx, true);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // send failed join event
        addEvent(clientId, new GameEvent(GameEventType.Join_error)
                .put("error", "Join failed.")
        );
    }

    /**
     * Make a guess.
     *
     * @param ctx
     */
    private void doGuess(RequestContext ctx) {
        var clientId = ctx.session();
        var guess = ctx.req().getParameter("str");
        var playerEntity = getEntity(PLAYER_TYPE, clientId);
        if (playerEntity != null) {
            var gameId = (String)playerEntity.getProperty("gameId");
            var gameState = getGameState(gameId); // get state
            var events = GameLogics.makeGuess(gameState, clientId, guess); // play
            putGameState(gameId, gameState); // store state
            // send events
            for (var event : events) {
                addEvent(event.target(), event.event());
            }
        }
    }

    /**
     * Set player word.
     *
     * @param ctx
     */
    private void setWord(RequestContext ctx) {
        var clientId = ctx.session();
        var word = ctx.req().getParameter("str");
        var playerEntity = getEntity(PLAYER_TYPE, clientId);
        if (playerEntity != null) {
            var gameId = (String) playerEntity.getProperty("gameId");
            var gameState = getGameState(gameId); // get state
            var events = GameLogics.setWord(gameState, clientId, word);
            putGameState(gameId, gameState); // store state
            for (var event : events) {
                addEvent(event.target(), event.event());
            }
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
        // iterate games
        var iter = datastore.prepare(new Query(GAME_TYPE)).asIterator();
        while (iter.hasNext()) {
            var entity = iter.next(); // get game entity
            var pass = (String) entity.getProperty("password");
            games.add(new GameInfo(
                    entity.getKey().getName(),
                    (String) entity.getProperty("name"),
                    pass != null
            ));
        }
        // list event
        var event = new GameEvent(GameEventType.Game_list)
                .setPayload(games);
        if (broadcast) {
            // broadcast to all
            for (var id : getAllIds(PLAYER_TYPE)) {
                addEvent(id, event);
            }
        } else {
            // send to caller
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
        var playerEntity = getEntity(PLAYER_TYPE, ctx.session());
        var event = new GameEvent(GameEventType.Player_list)
                .put("gameId", (String) playerEntity.getProperty("gameId"))
                .setPayload(players);
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
        String currentGameId = (String) playerEntity.getProperty("gameId");
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

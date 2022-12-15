package yh.fabulousstars.server;

import com.google.appengine.api.datastore.*;

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
        // api calls to respond to
        super(Arrays.asList("poll", "word", "guess", "connect", "disconnect", "leave", "join",
                "create", "message", "list"));
    }

    @Override
    protected void handleRequest(RequestContext ctx) throws IOException {
        // respond to the declared requests
        switch (ctx.endpoint()) {
            case "poll" -> poll(ctx);
            case "word" -> gameWord(ctx);
            case "guess" -> gameGuess(ctx);
            case "connect" -> lobbyConnect(ctx, true);
            case "disconnect" -> lobbyConnect(ctx, false);
            case "leave" -> gameLeave(ctx);
            case "join" -> lobbyJoin(ctx);
            case "create" -> lobbyCreate(ctx);
            case "message" -> message(ctx);
            case "listgames" -> listGames(ctx, false);
            case "listplayers" -> listPlayers(ctx, false);
        }
    }

    /**
     * Leave game and refresh player list for participants.
     *
     * events: list_players, leave
     * @param ctx
     */
    private void gameLeave(RequestContext ctx) {
        // get player
        var player = getEntity(PLAYER_TYPE, ctx.session());
        if(player == null) { return; }
        // get game id
        var gameId = player.getProperty("gameId").toString();
        if(gameId == null) { return; }
        // clear game id from player
        player.setProperty("gameId", null);
        datastore.put(player);
        // update player list
        listGamePlayers(ctx, gameId, "list_players");
        addEvent(ctx.session(), "leave", Map.of(
                "type", "leave",
                "json", String.format("{\"gameId\":\"%s\"}", gameId)
        ));
    }

    private void listGamePlayers(RequestContext ctx, String gameId, String eventName) {
        var query = new Query(PLAYER_TYPE)
                .setKeysOnly()
                .setFilter(new Query.FilterPredicate("gameId", Query.FilterOperator.EQUAL, gameId));
        var participants = datastore.prepare(query).asIterator();

        var players = getPlayersFromIterator(participants);
        var data = Map.of("type", eventName, "json", gson.toJson(players));
        // broadcast to participants
        for (var id : players) {
            addEvent(id.get("clientId"), eventName, data);
        }
    }

    /**
     * Client connect.
     *
     * events: connected, connect_error
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
                var map = new HashMap<String, String>();
                map.put("name", name);
                map.put("gameId", null);
                // store in db
                var entity = new Entity(PLAYER_TYPE, clientId);
                setProperties(entity, map);
                datastore.put(entity);
                addEvent(clientId, "connected", map);
                listPlayers(ctx, true);
            } else {

                // name not ok. empty response
                addEvent(clientId, "connect_error",
                        Map.of("error", "Name error."));
            }
        } else {
            // disconnect
            deleteClient(clientId);
            listPlayers(ctx, true);
        }
    }

    /**
     * Create game from lobby and join it.
     *
     * events: "created", "create_error"
     * @param ctx
     */
    private void lobbyCreate(RequestContext ctx) {
        // get player
        Entity player = getEntity(PLAYER_TYPE, ctx.session());
        if(player == null) { return; }
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
                // create game
                var entity = new Entity(GAME_TYPE);
                entity.setProperty("name", name);
                entity.setProperty("password", pass);
                entity.setProperty("owner", ctx.session());
                var key = datastore.put(entity); // game id from db
                // get entity as map
                var game = getStringProperties(entity);
                game.put("gameId", key.getName()); // set game id
                addEvent(ctx.session(), "created", game); // send game created to player
                listGames(ctx, true); // broadcast list of games
                return;
            }
        }
        // fail
        addEvent(ctx.session(), "create_error", Map.of("error", "Error creating game."));
    }

    /**
     * Join a game.
     *
     * events: join, game_list
     * @param ctx
     */
    private void lobbyJoin(RequestContext ctx) {
        // get session
        var clientId = ctx.session();
        // get params
        var gameId = ctx.req().getParameter("game");
        var password = ctx.req().getParameter("pass");
        if(gameId!=null) {
            if(password==null) { password="";}
            try {
                gameId = gameId.trim();
                // get game from db
                var gameEntity = datastore.get(KeyFactory.createKey(GAME_TYPE, gameId));
                // if db pass exists and user pass is correct
                var pw = gameEntity.getProperty("password");
                if (pw==null || password.equals(pw)) {
                    // check if game is full
                    var query = new Query(PLAYER_TYPE)
                            .setFilter(new Query.FilterPredicate("gameId", Query.FilterOperator.EQUAL, gameId))
                            .setKeysOnly();
                    var currentPlayerCount = datastore.prepare(query).countEntities(FetchOptions.Builder.withDefaults());
                    if(currentPlayerCount>=MAX_PLAYERS_PER_GAME) {
                        addEvent(clientId, "join", Map.of(
                                "error", "Game is full."
                        ));
                        return;
                    }
                    // update player
                    var playerEntity = datastore.get(KeyFactory.createKey(PLAYER_TYPE, clientId));
                    playerEntity.setProperty("gameId", gameId);
                    datastore.put(playerEntity);
                    // send join event
                    var gameMap= getStringProperties(gameEntity);
                    gameMap.put("gameId", gameId);
                    addEvent(clientId, "join", Map.of(
                        "json", gson.toJson(gameMap)
                    ));
                    // broadcast list of games
                    listGames(ctx,true); // broadcast game list
                    listGamePlayers(ctx, gameId, "player_list");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // send failed join event
        addEvent(clientId, "join", Map.of(
            "error", "Join failed."
        ));
    }


    private void gameGuess(RequestContext ctx) {
        //TODO: gameGuess()
    }

    private void gameWord(RequestContext ctx) {
        //TODO: gameWord
    }

    private void message(RequestContext ctx) {
        var clientId = ctx.session();
        // get params
        var gameId = ctx.req().getParameter("gameId");
        var message = ctx.req().getParameter("message");
        var query = new Query(PLAYER_TYPE)
                .setFilter(new Query.FilterPredicate("gameId", Query.FilterOperator.EQUAL, gameId))
                .setKeysOnly();
        var participants = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        for (var participant: participants) {
            addEvent(clientId, "message", Map.of(
                    "message", message,
                    "inGame", gameId==null ? "0" : "1"
            ));
        }
    }

    /**
     * Create a list_games event.
     *
     * events: game_list
     * @param ctx
     */
    private void listGames(RequestContext ctx, boolean broadcast) {
        // results
        var games = new ArrayList<Map<String, String>>();
        // iterate database games
        var iter = datastore.prepare(new Query(GAME_TYPE)).asIterator();
        while (iter.hasNext()) {
            var entity = iter.next(); // get datastore entity
            var game = Map.of(
                    "name", entity.getProperty("name").toString(),
                    "gameId", entity.getKey().getName()
            );
            games.add(game);
        }
        var data = Map.of("type", "game_list", "json", gson.toJson(games));
        if(broadcast) {
            // broadcast pollable event
            for(var id : getAllIds(PLAYER_TYPE)) {
                addEvent(id, "game_list", data);
            }
        } else {
            // create pollable event for caller
            addEvent(ctx.session(), "game_list", data);
        }
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
     * List players.
     * @param ctx
     * @return list of players
     */
    private void listPlayers(RequestContext ctx, boolean broadcast) {
        var players = new LinkedList<Map<String, String>>();
        var iter = datastore.prepare(new Query(PLAYER_TYPE)).asIterator();
        while (iter.hasNext()) {
            var entity = iter.next(); // get datastore entity
            var player = Map.of(
                    "name", entity.getProperty("name").toString(),
                    "clientId", entity.getKey().getName()
            );
            players.add(player);
        }
        var data = Map.of("type", "player_list", "json", gson.toJson(players));
        if(broadcast) {
            // broadcast pollable event
            for (var id : getAllIds(PLAYER_TYPE)) {
                addEvent(id, "player_list", data);
            }
        } else {
            // create pollable event for caller
            addEvent(ctx.session(), "player_list", data);
        }
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

    private List<Map<String,String>> getPlayersFromIterator(Iterator<Entity> iter) {
        var players = new LinkedList<Map<String, String>>();
        while (iter.hasNext()) {
            var entity = iter.next(); // get datastore entity
            var player = Map.of(
                    "name", entity.getProperty("name").toString(),
                    "clientId", entity.getKey().getName()
            );
            players.add(player);
        }
        return players;
    }
}

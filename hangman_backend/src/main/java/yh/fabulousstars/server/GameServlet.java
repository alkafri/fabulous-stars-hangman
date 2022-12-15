package yh.fabulousstars.server;

import com.google.appengine.api.datastore.*;
import yh.fabulousstars.server.models.Game;
import yh.fabulousstars.server.models.Player;
import yh.fabulousstars.server.utils.EntityUtils;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.*;

/**
 * Game servlet handles game instance requests.
 */
@WebServlet(name = "GameServlet", value = "/api/*")
public class GameServlet extends BaseServlet {
    public GameServlet() {
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
            case "leave" -> gameLeave(ctx, false);
            case "join" -> lobbyJoin(ctx);
            case "create" -> lobbyCreate(ctx);
            case "message" -> message(ctx);
            case "listgames" -> listGames(ctx, false);
            case "listplayers" -> listPlayers(ctx, false);
        }
    }

    private void gameLeave(RequestContext ctx, boolean b) {
    }

    /**
     * Client connect.
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
     * Create game from lobby.
     *
     * @param ctx
     */
    private void lobbyCreate(RequestContext ctx) {
        // get session
        var clientId = ctx.session();
        Entity player = null;
        try {
            player = datastore.get(KeyFactory.createKey(PLAYER_TYPE,clientId));
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return;
        }
        if (player.getProperty("gameId") == null) {
            var name = checkName(ctx.req().getParameter("name"), GAME_TYPE);
            var pass = ctx.req().getParameter("password");
            if (name != null) {
                if (pass != null) {
                    pass = pass.trim();
                    if (pass.equals("")) pass = null;
                }
                var entity = new Entity(GAME_TYPE);
                entity.setProperty("name", name);
                entity.setProperty("password", pass);
                entity.setProperty("owner", clientId);
                var key = datastore.put(entity);
                var game = getStringProperties(entity);
                game.put("gameId", key.getName());
                addEvent(clientId, "created", game);
                listGames(ctx, true);
                return;
            }
        }
        addEvent(clientId, "create_error", Map.of("error", "Error creating game."));
    }

    /**
     * Join a game
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
                    //TODO: check if game is full

                    // update palyer
                    var playerEntity = datastore.get(KeyFactory.createKey(PLAYER_TYPE, clientId));
                    playerEntity.setProperty("gameId", gameEntity.getKey().getId());
                    datastore.put(playerEntity);
                    // send join event
                    var gameMap= getStringProperties(gameEntity);
                    gameMap.put("gameId",gameEntity.getKey().getName());
                    addEvent(clientId, "join", Map.of(
                        "json", gson.toJson(gameMap)
                    ));
                    // broadcast list of games
                    listGames(ctx,true); // broadcast game list
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
        //TODO: message
    }

    /**
     * Create a list_games event.
     *
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
        var data = Map.of("type", "list_games", "json", gson.toJson(games));
        if(broadcast) {
            // broadcast pollable event
            for(var id : getAllClients()) {
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
     * List games.
     * @param ctx
     * @return list of games
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
            for (var id : getAllClients()) {
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

}

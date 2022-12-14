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
            case "list" -> listGames(ctx);
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
        var clientId = ctx.session().getId();

        // connection or disconnection?
        if (connect) {
            // connection request. check player name
            var name = checkName(ctx.req().getParameter("name"), PLAYER_TYPE);
            if (name != null) {
                var map = new HashMap<String, String>();
                map.put("name", name);
                map.put("gameId", null);
                map.put("clientId", clientId);
                // store in db
                var entity = new Entity(PLAYER_TYPE, clientId);
                setProperties(entity, map);
                datastore.put(entity);
                addEvent(clientId, "connected", map);
            } else {

                // name not ok. empty response
                addEvent(clientId, "connect_error",
                        Map.of("error", "Name error."));
            }
        } else {
            // disconnect
            deleteClient(clientId);
            ctx.session().invalidate();
        }
    }

    /**
     * Create game from lobby.
     *
     * @param ctx
     */
    private void lobbyCreate(RequestContext ctx) {
        var clientId = ctx.session().getId();
        var player = getPlayer(clientId);
        if (player.gameId != null) {
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
                return;
            }
        }
        addEvent(clientId, "create_error", Map.of("error", "Error creating game."));
    }

    private void lobbyJoin(RequestContext ctx) {

    }


    private void gameGuess(RequestContext ctx) {
    }

    private void gameWord(RequestContext ctx) {
    }

    private void message(RequestContext ctx) {
    }

    /**
     * Create a list_games event.
     *
     * @param ctx
     */
    private void listGames(RequestContext ctx) {
        // results
        var games = new ArrayList<Map<String, String>>();
        // iterate database games
        var iter = datastore.prepare(new Query(GAME_TYPE)).asIterator();
        while (iter.hasNext()) {
            var entity = iter.next(); // get datastore entity
            var game = getStringProperties(entity);
            game.put("gameId", entity.getKey().getName());
            games.add(game);
        }
        var data = Map.of("type", "list_games", "json", gson.toJson(games));
        // create pollable event
        addEvent(ctx.session().getId(), "list_games", data);
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

    protected Player getPlayer(String clientId) {
        var key = KeyFactory.createKey(PLAYER_TYPE, clientId);
        try {
            var entity = datastore.get(key);
            var player = new Player();
            player.clientId = clientId;
            EntityUtils.setFromEntity(entity, player);
            return player;
        } catch (Exception e) {
            log(e.getMessage());
        }
        return null;
    }

    /**
     * @param ctx
     * @param gameId
     * @return
     */
    private List<Map<String, String>> listPlayers(RequestContext ctx, String gameId) {
        var players = new LinkedList<Map<String, String>>();
        var query = new Query(PLAYER_TYPE);
        var iter = datastore.prepare(
                query.setFilter(new Query.FilterPredicate(
                        "gameId", Query.FilterOperator.EQUAL, gameId))
        ).asIterator();
        while (iter.hasNext()) {
            var entity = iter.next();
            var player = getStringProperties(entity);
            player.put("clientId", entity.getKey().getName());
            players.add(player);
        }
        return players;
    }

    /**
     * Get game.
     *
     * @param gameId game id
     * @return Game object
     */
    protected Game getGame(String gameId) {
        Entity entity = null;
        try {
            entity = datastore.get(KeyFactory.createKey("Game", gameId));
        } catch (EntityNotFoundException e) {
            return null;
        }
        var game = new Game();
        EntityUtils.setFromEntity(entity, game);
        return game;
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

package yh.fabulousstars.server;

import com.google.appengine.api.datastore.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import yh.fabulousstars.server.models.Game;
import yh.fabulousstars.server.models.GameEvent;
import yh.fabulousstars.server.models.Player;
import yh.fabulousstars.server.utils.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseServlet extends HttpServlet {
    protected static final String PLAYER_TYPE = "Player";
    protected static final String GAME_TYPE = "Game";
    protected static final String EVENT_TYPE = "Event";
    protected final DatastoreService datastore;
    protected final Gson gson;
    private final ArrayList<String> endpoints;

    public BaseServlet(Collection<String> endpoints) {
        super();
        this.datastore = DatastoreServiceFactory.getDatastoreService();
        this.gson = new GsonBuilder()
                .serializeNulls()
                .create();
        this.endpoints = new ArrayList<>(endpoints);
    }

    /**
     * Return valid endpoint and set content type.
     *
     * @param req
     * @param resp
     * @return endpoint
     * @throws IOException
     */
    private RequestContext setup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "application/json");
        var path = req.getPathInfo();
        var endpoint = path!=null ? req.getPathInfo().substring(1) : "";
        if (endpoints.contains(endpoint)) {
            return new RequestContext(endpoint, req.getSession(), req, resp);
        }
        throw new FileNotFoundException(endpoint);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleRequest(setup(req, resp));
    }


    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleRequest(setup(req, resp));
    }

    protected void objectToJsonStream(RequestContext ctx, Object obj) throws IOException {
        gson.toJson(obj, obj.getClass(), ctx.resp().getWriter());
    }

    protected Object objectFromJsonStream(RequestContext ctx, Type T) throws IOException {
        return gson.fromJson(
                new InputStreamReader(ctx.req().getInputStream()),
                T
        );
    }

    protected Player getPlayer(RequestContext ctx) {
        var key = KeyFactory.createKey(PLAYER_TYPE, ctx.session().getId());
        try {
            var entity = datastore.get(key);
            var player = new Player();
            player.clientId = key.getName();
            EntityUtils.setFromEntity(entity, player);
            return player;
        } catch (Exception e) {
            log(e.getMessage());
        }
        return null;
    }

    /**
     * Create player list event.
     * @param ctx
     * @param gameId GAme or null for players not in a game.
     */
    private void listPlayersEvent(RequestContext ctx, String gameId) {
        var players = listPlayers(ctx, gameId);
        addEvent(ctx.session().getId(),"list_players", gson.toJson(players));
    }
    private List<Player> listPlayers(RequestContext ctx, String gameId) {
        var players = new ArrayList<Player>();
        var query = new Query(PLAYER_TYPE);
        var iter = datastore.prepare(
                query.setFilter(new Query.FilterPredicate(
                        "gameId", Query.FilterOperator.EQUAL, gameId))
        ).asIterator();
        while(iter.hasNext()) {
            var entity = iter.next();
            var player = new Player();
            player.clientId = entity.getKey().getName();
            EntityUtils.setFromEntity(entity, player);
            players.add(player);
        }
        return players;
    }

    protected Game getGame(Player player) {
        throw new RuntimeException();
    }

    protected void addEvent(String clientId, String eventName, Object data)
    {
        var entity = new Entity(EVENT_TYPE);
        entity.setProperty("clientId", clientId);
        entity.setProperty("created", System.currentTimeMillis());
        entity.setProperty("type", eventName);
        entity.setProperty("json", gson.toJson(data));
        datastore.put(entity);
    }

    /**
     * Poll and return oldest event.
     * @param ctx
     * @throws IOException
     */
    protected void poll(RequestContext ctx) throws IOException {
        var entity = datastore.prepare(
                new Query(EVENT_TYPE)
                        .addSort("created")
        ).asSingleEntity();
        if(entity != null) {
            var event = new GameEvent();
            EntityUtils.setFromEntity(entity, event);
            objectToJsonStream(ctx, event);
        }
    }

    protected abstract void handleRequest(RequestContext ctx) throws IOException;
}

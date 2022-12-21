package yh.fabulousstars.hangman.server;

import com.google.appengine.api.datastore.*;
import yh.fabulousstars.hangman.game.GameEvent;
import yh.fabulousstars.hangman.game.GameEventType;
import yh.fabulousstars.hangman.game.GameState;
import yh.fabulousstars.hangman.server.utils.EntityUtils;
import yh.fabulousstars.hangman.utils.ObjectHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class BaseServlet extends HttpServlet {
    public static final String PLAYER_TYPE = "Player";
    public static final String GAME_TYPE = "Game";
    protected static final String GAME_STATE_TYPE = "GameState";
    protected static final String EVENT_TYPE = "Event";
    private static final long ENTITY_EXPIRY_MS = 30 * 60 * 1000; // min * sec * millis
    protected final DatastoreService datastore; // google datastore service api

    protected BaseServlet() {
        super();
        this.datastore = DatastoreServiceFactory.getDatastoreService(
                DatastoreServiceConfig.Builder.withDefaults()
        );
    }

    /**
     * Handle GET
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        var session = req.getSession();
        var path = req.getPathInfo();
        if (path != null) {
            var endpoint = req.getPathInfo().substring(1);
            handleRequest(new RequestContext(endpoint, session.getId(), req, resp));
        } else {
            resp.sendRedirect("/");
        }
    }

    protected void setExpiry(Entity entity) {
        entity.setProperty("expires", new Date(System.currentTimeMillis() + ENTITY_EXPIRY_MS));
    }

    /**
     * Get entity from database.
     *
     * @param type
     * @param id
     * @return entity
     */
    protected Entity getEntity(String type, String id) {
        try {
            return datastore.get(
                    KeyFactory.createKey(type, id)
            );
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * Gut stati into db.
     *
     * @param gameId
     * @return
     */
    protected GameState getGameState(String gameId) {
        var entity = getEntity(GAME_STATE_TYPE, gameId);
        if (entity != null) {
            return (GameState) EntityUtils.getBlobObject(entity);
        }
        return null;
    }

    /**
     * Gut stati into db.
     *
     * @param gameId
     */
    protected void putGameState(String gameId, GameState gameState) {
        var entity = new Entity(GAME_STATE_TYPE, gameId);
        setExpiry(entity);
        EntityUtils.putBlobObject(entity, gameState);
        datastore.put(entity);
    }

    /**
     * Get all entity ids.
     *
     * @return list of ids
     */
    protected List<String> getAllIds(String type) {
        var ids = new ArrayList<String>();
        var iter = datastore.prepare(new Query(type).setKeysOnly()).asIterator();
        while (iter.hasNext()) {
            var entity = iter.next();
            ids.add(entity.getKey().getName());
        }
        return ids;
    }

    /**
     * Add event to database for polling.
     *
     * @param clientId target client.
     * @param event    Event object
     */
    protected void addEvent(String clientId, GameEvent event) {
        var entity = new Entity(EVENT_TYPE);
        entity.setProperty("target", clientId); // client
        entity.setProperty("created", System.currentTimeMillis());
        setExpiry(entity);
        EntityUtils.putBlobObject(entity, event);
        System.out.println("ADD_EVENT: " + event.getType().toString() + " -> " + clientId);
        datastore.put(entity);
    }

    /**
     * Poll and return oldest event, removing it from database.
     * The event is sent to the client as a json body.
     *
     * @param ctx
     * @throws IOException
     */
    protected void poll(RequestContext ctx) throws IOException {

        ctx.resp().setContentType("application/octet-stream");
        var output = ctx.resp().getOutputStream();

        // query first
        var entityIter = datastore.prepare(
                new Query(EVENT_TYPE)
                        .setFilter(new Query.FilterPredicate(
                                "target", Query.FilterOperator.EQUAL, ctx.session())
                        )
                        .addSort("expires", Query.SortDirection.ASCENDING)
        ).asIterator();
        if (entityIter.hasNext()) {
            var entity = entityIter.next();
            var bytes = EntityUtils.getBlobBytes(entity);
            datastore.delete(entity.getKey());
            output.write(bytes);
            return;
        }
        // empty
        var idle = ObjectHelper.toBytes(new GameEvent(GameEventType.Idle));
        output.write(idle);
    }

    /**
     * Handle requests from get and put in sub-class.
     *
     * @param ctx
     * @throws IOException
     */
    protected abstract void handleRequest(RequestContext ctx) throws IOException;
}

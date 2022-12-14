package yh.fabulousstars.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {
    protected static final String PLAYER_TYPE = "Player";
    protected static final String GAME_TYPE = "Game";
    protected static final String EVENT_TYPE = "Event";
    protected final DatastoreService datastore; // google datastore service api
    protected final Gson gson; // google json serializer
    /**
     * List of endpoints to respond to.
     */
    private final ArrayList<String> endpoints;

    /**
     *
     * @param endpoints
     */
    protected BaseServlet(Collection<String> endpoints) {
        super();
        this.datastore = DatastoreServiceFactory.getDatastoreService();
        this.gson = new GsonBuilder()
                .serializeNulls()
                .create();
        this.endpoints = new ArrayList<>(endpoints);
    }

    /**
     * Return valid endpoint and set content type to json.
     *
     * @param req
     * @param resp
     * @return endpoint
     * @throws IOException
     */
    private RequestContext setup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "application/json");
        var path = req.getPathInfo();
        var endpoint = path != null ? req.getPathInfo().substring(1) : "";
        if (endpoints.contains(endpoint)) {
            return new RequestContext(endpoint, req.getSession(), req, resp);
        }
        throw new FileNotFoundException(endpoint);
    }

    /**
     * Handle GET
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleRequest(setup(req, resp));
    }

    /**
     * Handle PUT
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleRequest(setup(req, resp));
    }

    /**
     * Send object as json.
     *
     * @param ctx
     * @param obj
     * @throws IOException
     */
    protected void objectToJsonStream(RequestContext ctx, Object obj) throws IOException {
        gson.toJson(obj, obj.getClass(), ctx.resp().getWriter());
    }

    /**
     * Set entity properties from a source map.
     *
     * @param entity
     * @param source
     */
    protected void setProperties(Entity entity, Map<String, String> source) {
        for (var entry : source.entrySet()) {
            entity.setProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Convert Entity properties to map of strings.
     *
     * @param entity
     * @return
     */
    protected Map<String, String> getStringProperties(Entity entity) {
        Map<String, String> map = new HashMap<>();
        entity.getProperties().forEach((k, v) -> map.put(k, v==null ? null : v.toString()));
        return map;
    }


    /**
     * Add event to database for polling.
     *
     * @param clientId target client.
     * @param eventName String id fro event.
     * @param data event data.
     */
    protected void addEvent(String clientId, String eventName, Map<String, String> data) {
        var entity = new Entity(EVENT_TYPE);
        entity.setProperty("targetId", clientId); // client
        entity.setProperty("created", System.currentTimeMillis());
        entity.setProperty("type", eventName);
        setProperties(entity, data);
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
        // query first
        var entity = datastore.prepare(
                new Query(EVENT_TYPE)
                        .addSort("created")
        ).asSingleEntity();
        if (entity != null) {
            var map = getStringProperties(entity);
            // remove backend info
            map.remove("targetId");
            map.remove("created");
            // send
            objectToJsonStream(ctx, map);
            // remove from db
            datastore.delete(entity.getKey());
        }
    }

    /**
     * Handle requests from get and put in sub-class.
     * @param ctx
     * @throws IOException
     */
    protected abstract void handleRequest(RequestContext ctx) throws IOException;
}

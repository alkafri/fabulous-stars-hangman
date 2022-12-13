package yh.fabulousstars.server;

import com.google.appengine.api.datastore.Query;
import yh.fabulousstars.server.models.Game;
import yh.fabulousstars.server.models.Player;
import yh.fabulousstars.server.utils.EntityUtils;

import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Game servlet handles game instance requests.
 */
@WebServlet(name = "GameServlet", value = "/game/*")
public class GameServlet extends BaseServlet {
    public GameServlet() {
        super(Arrays.asList("word", "guess", "quit", "message"));
    }

    @Override
    protected void handleRequest(RequestContext ctx) {

    }
}

/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yh.fabulousstars.server;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import yh.fabulousstars.server.models.Game;
import yh.fabulousstars.server.models.Player;
import yh.fabulousstars.server.utils.EntityUtils;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Lobby servlet handles requests outside game instance.
 */
@SuppressWarnings("serial")
@WebServlet(name = "LobbyServlet", value = "/lobby/*")
public class LobbyServlet extends BaseServlet {
    public LobbyServlet() {
        // api methods to respond to
        super(Arrays.asList("connect", "disconnect", "create", "poll", "message"));
    }

    @Override
    protected void handleRequest(RequestContext ctx) throws IOException {

        // respond to the declared requests
        switch (ctx.endpoint()) {
            case "poll" -> poll(ctx);
            case "connect" -> lobbyConnect(ctx, true);
            case "disconnect" -> lobbyConnect(ctx, false);
            case "create" -> createGame(ctx);
            case "message" -> message(ctx);
        }
    }

    /**
     * create game.
     *
     * @param ctx
     */
    private void createGame(RequestContext ctx) {
    }

    /**
     * lobby message.
     *
     * @param ctx
     */
    private void message(RequestContext ctx) {

    }

    /**
     * Client connect.
     *
     * @param ctx
     * @param connect
     * @throws IOException
     */
    private void lobbyConnect(RequestContext ctx, boolean connect) throws IOException {
        // result
        var player = new Player();
        // connection or disconnection?
        if (connect) {
            // connection request. check player name
            var name = checkName(ctx.req().getParameter("name"));
            if (name!=null) {
                // name is ok. set response
                player.clientId = ctx.session().getId();
                player.name = name;
                // store in db
                var entity = new Entity(PLAYER_TYPE, player.clientId);
                entity.setProperty("name", name);
                datastore.put(entity);
            } else {
                // name not ok. empty response
            }
        } else {
            // disconnect
            ctx.session().invalidate();
            //TODO: disconnectPlayer();
        }
        objectToJsonStream(ctx, player);
    }

    /**
     * Create a list_games event.
     * @param ctx
     */
    private void listGames(RequestContext ctx) {
        // results
        var games = new ArrayList<Game>();
        // iterate database games
        var iter = datastore.prepare(new Query(GAME_TYPE)).asIterator();
        while(iter.hasNext()) {
            var entity = iter.next(); // get datastore entity
            var game = new Game(); // game object
            game.gameId = entity.getKey().getName(); // set game id
            EntityUtils.setFromEntity(entity, game); // copy from entity
            games.add(game);
        }
        // create pollable event
        addEvent(ctx.session().getId(),"list_games", gson.toJson(games));
    }

    /**
     * Check that name is at least 2 characters and doesn't exist.
     * @param name name to check
     * @return name trimmed name
     */
    private String checkName(String name) {
        if(name != null) {
            name = name.trim();
            // at least 2 chars
            if(name.length() >= 2) {
                // check for existing
                var query = new Query(PLAYER_TYPE)
                        .setFilter(new Query.FilterPredicate(
                                "name",
                                Query.FilterOperator.EQUAL,
                                name));
                if(datastore.prepare(query).asSingleEntity() == null) {
                    return name;
                }
            }
        }
        return null;
    }
}

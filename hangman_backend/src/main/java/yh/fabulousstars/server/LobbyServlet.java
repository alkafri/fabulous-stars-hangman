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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Lobby servlet handles requests outside game instance.
 * */
@SuppressWarnings("serial")
@WebServlet(name = "LobbyServlet", value = "/lobby")
public class LobbyServlet extends HttpServlet {
    private DatastoreService datastore;
    private Gson gson;

  // Process the HTTP POST of the form
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

	var out = resp.getWriter();
      out.println("getPathInfo: "+ req.getMethod());
      out.println("getServletPath: "+ req.getServletPath());
      out.println("getPathInfo: "+ req.getPathInfo());
      out.println("getQueryString: "+ req.getQueryString());
      out.println("DatastoreService: "+ datastore.toString());
  }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        datastore = DatastoreServiceFactory.getDatastoreService();
        gson = new GsonBuilder().create();
    }
}

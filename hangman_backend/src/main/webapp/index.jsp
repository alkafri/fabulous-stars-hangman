<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="java.util.List" %>
<%@ page import="yh.fabulousstars.hangman.server.BaseServlet" %>

<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <link href='//fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
  <title>Fabulous Backend</title>
</head>
<body>

<h1>Fabulous Backend</h1>
<%
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
    List<Entity> games = datastore.prepare(new Query(BaseServlet.PLAYER_TYPE)).asList(fetchOptions);
    List<Entity> players = datastore.prepare(new Query(BaseServlet.GAME_TYPE)).asList(fetchOptions);
%>
<hr />
<h2>Game instances:</h2>
<ul>
<%  for(Entity game : games) { %>
    <li><% game.getProperty("name"); %></li>
<%  } %>
</ul>
<hr />
<h2>Server players:</h2>
<ul>
<%  for(Entity player : players) { %>
    <li><% player.getProperty("name"); %></li>
    <%  } %>
</ul>

</body>
</html>

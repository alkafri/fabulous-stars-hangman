<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="yh.fabulousstars.hangman.server.BaseServlet" %>
<%@ page import="java.util.Iterator" %>

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
%>
<hr/>
<h2>Game instances:</h2>
<ul>
    <%
        Iterator<Entity> games = datastore.prepare(new Query(BaseServlet.GAME_TYPE)).asIterator();
        while (games.hasNext()) {
            Entity game = games.next();
    %>
    <li><%= game.getProperty("name").toString() %>
    </li>
    <% } %>
</ul>
<hr/>
<h2>Server players:</h2>
<ul>
    <%
        Iterator<Entity> players = datastore.prepare(new Query(BaseServlet.PLAYER_TYPE)).asIterator();
        while (players.hasNext()) {
            Entity player = players.next();
    %>
    <li><%= player.getProperty("name").toString() %>
    </li>
    <% } %>
</ul>

</body>
</html>

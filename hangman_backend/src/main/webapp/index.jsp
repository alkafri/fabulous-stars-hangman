<%@ page import="yh.fabulousstars.server.models.*" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.google.appengine.api.datastore.*" %>

<!--
Copyright 2019 Google LLC
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
      http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->


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
<h2>Running games:</h2>
<%
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Game");
    query.addSort("expires");
    Iterator<Entity> games = datastore.prepare(query).asIterator();
    while(games.hasNext()) {
        Entity game = games.next();
%>
<p><%
        game.getProperty("name");
%></p>
<%
    }
%>

</body>
</html>

<%@ page 
  language="java" 
  contentType="text/html; charset=utf-8"
  import="java.sql.Timestamp,
          java.text.SimpleDateFormat,
          net.ontopia.utils.StringUtils,
          net.ontopia.topicmaps.utils.sdshare.client.*"
%><%@ taglib prefix="c"        uri="http://java.sun.com/jsp/jstl/core" %><%!

  private static SimpleDateFormat format = 
    new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

  private static String format(long time) {
    if (time == 0)
      return "&nbsp;";
    else
      return format.format(time);
  }

  private static String format(Timestamp time) {
    if (time == null)
      return "&nbsp;";
    else
      return time.toString() + 'Z';
  }

%><%
  ClientManager manager = (ClientManager) getServletContext().getAttribute("client-manager");
  if (manager == null) {
    ClientConfig cconfig = ClientConfig.readConfig();
    manager = new ClientManager(cconfig, Utils.getWebappName(request));
    getServletContext().setAttribute("client-manager", manager);
  }
%>

<script>
function swap(rowix) {
  var row = document.getElementById("actionrow" + rowix);
  var button = document.getElementById("button" + rowix);

  if (row.className == "hidden") {
    row.className = "visible";
    button.value = "-";
  } else {
    row.className = "hidden";
    button.value = "+";
  }
}

</script>

<style>th { text-align: left; }
td, th { padding-right: 6pt }
.hidden { display: none }</style>
<title>Ontopia SDshare client</title>

<h1>SDshare client</h1>

<form action="action.jsp" method="post">
<p><b>State:</b> 
<%= manager.getStatus() %>
<% if (manager.isStopped()) { 
     if (manager.getConfig().getStartButton()) { %>
       <input type=submit name=start value="Start">
     <% } %>
    <input type=submit name=sync value="Sync">
<% } else if (manager.isRunning()) { %>
<input type=submit name=stop value="Stop">
<% } %>
</p>

<p>Endpoints to synchronize into:</p>

<%
  ClientConfig cconfig = manager.getConfig();
  int ix = 0;
  for (SyncEndpoint endpoint : cconfig.getEndpoints()) { %>
    <h2><%= endpoint.getHandle() %></h2>
    <p><%= endpoint.getBackend() %></p>

    <table>
    <tr><th>Source <th>Last change <th>Last sync <th>Fragments

    <% for (SyncSource ss : endpoint.getSources()) { %>
      <tr><td><%= ss.getHandle() %>
          <td><%= format(ss.getLastChange()) %>
          <td><%= format(ss.getLastCheck()) %>
          <td><%= ss.getFragmentCount() %>
	  <td><input type=button value="+" onclick="javascript:swap(<%= ix %>);"
                     id=button<%= ix %>>
      <%
        if (ss.isBlockedByError()) {
      %>
        <tr><td colspan=4><span style="color: red">
              <b><%= StringUtils.escapeHTMLEntities(ss.getError()) %></b>
            </span> <br>
        <input type=submit name=clear<%= ix %> value="Clear">
     <% } %>

      <tr id="actionrow<%= ix %>" class=hidden
          ><td colspan=4>
          <input type=button name=stop<%= ix %> value="Stop" disabled>
          <input type=button name=restart<%= ix %> value="Restart" disabled>
          <input type=submit name=snapshot<%= ix %> value="Snapshot">
          <input type=hidden name=id<%= ix++ %> 
            value="<%= endpoint.getHandle() %> <%= ss.getHandle() %>">

      <%
        if (ss.isActive()) {
      %>
        <tr><td colspan=4><span style="color: green">
              <b>currently active</b></span>
     <% } %>
   <% } %>
   </table>
  <% } %>

<input type=hidden name=number value="<%= ix %>">
</form>

<%--
This JSP is used for checking the health of the SDshare client. It returns
either 200 (meaning all is well) or 500 (meaning there is something wrong).
--%><%@ page 
  language="java" 
  contentType="text/html; charset=utf-8"
  import="java.text.SimpleDateFormat,
          net.ontopia.utils.StringUtils,
          net.ontopia.topicmaps.utils.sdshare.client.*"
%><%

  ClientManager manager = (ClientManager) getServletContext().getAttribute("client-manager");
  if (manager == null) {
    ClientConfig cconfig = ClientConfig.readConfig();
    manager = new ClientManager(cconfig, Utils.getWebappName(request));
    getServletContext().setAttribute("client-manager", manager);
  }

  // first check if the thread is running
  if (!manager.isRunning()) {
    response.sendError(500, "Status: " + manager.getStatus());
    return;
  }

  ClientConfig cconfig = manager.getConfig();
  int sources = 0;
  int ok = 0;
  for (SyncEndpoint endpoint : cconfig.getEndpoints()) {
    for (SyncSource ss : endpoint.getSources()) {
      sources++;
      if (!ss.isBlockedByError())
        ok++;
    }
  }

  if (sources != ok)
    response.sendError(500, "" + sources + " sources, but only " + ok + " OK");
  // if we don't do anything the server sends "200 OK" for us
%>

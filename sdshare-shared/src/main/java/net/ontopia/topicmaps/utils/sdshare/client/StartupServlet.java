
package net.ontopia.topicmaps.utils.sdshare.client;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBLIC: This servlet loads at start up (if configured to do so in
 * web.xml) starts up the client's polling thread so that starting the
 * server is enough to also start the SDshare sync process.
 */
public class StartupServlet extends HttpServlet {
  static Logger log = LoggerFactory.getLogger(StartupServlet.class.getName());

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    log.info("Starting SDshare client setup servlet");

    ClientConfig cconfig = ClientConfig.readConfig();
    ClientManager manager =
      new ClientManager(cconfig, config.getInitParameter("app-name"));
    getServletContext().setAttribute("client-manager", manager);

    manager.startThread();
  }

  public void destroy() {
    ClientManager manager = (ClientManager)
      getServletContext().getAttribute("client-manager");
    manager.shutdown();
  }
}
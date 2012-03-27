
package net.ontopia.topicmaps.utils.sdshare.client;

import javax.servlet.http.HttpServletRequest;

public class Utils {

  public static String getWebappName(HttpServletRequest request) {
    // we need a unique name for this webapp instance. using first part
    // of document path inside server.
    String url = request.getRequestURL().toString();
    int lastslash = url.lastIndexOf('/');
    int secondlast = url.lastIndexOf('/', lastslash - 1);

    if (secondlast < 7) // means we're looking at http://
      return "ROOT";
    else
      return url.substring(secondlast + 1, lastslash);
  }
}
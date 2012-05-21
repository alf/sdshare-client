
package net.ontopia.topicmaps.utils.sdshare.client;

/**
 * INTERNAL: Backend which supports the version of the SPARQL Update
 * protocol used by Joseki 3.4.4.
 */
public class JosekiSparqlBackend extends SparqlBackend {

  protected String getParameterName() {
    return "request";
  }

}
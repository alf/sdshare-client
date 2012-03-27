
package net.ontopia.topicmaps.utils.sdshare.client;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.io.IOException;

import org.xml.sax.SAXException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBLIC: Represents an endpoint which sources are synchronized
 * into. What exactly the endpoint is depends on the backend used.  In
 * the Ontopia backend it is a topic map, while in the SPARQL backend
 * it's a triple store.
 */
public class SyncEndpoint {
  private String handle;
  private Collection<SyncSource> sources;
  private Map<String, String> properties;
  private ClientBackendIF backend;
  static Logger log = LoggerFactory.getLogger(SyncEndpoint.class.getName());

  public SyncEndpoint(String handle) {
    this.handle = handle;
    this.sources = new ArrayList<SyncSource>();
    this.properties = new HashMap();
  }

  public void addSource(SyncSource source) {
    sources.add(source);
    source.setEndpoint(this);
  }

  public String getHandle() {
    return handle;
  }

  public Collection<SyncSource> getSources() {
    return sources;
  }

  public String getProperty(String name) {
    return properties.get(name);
  }

  public void setProperty(String name, String value) {
    properties.put(name, value);
  }

  public ClientBackendIF getBackend() {
    return backend;
  }

  public void setBackend(ClientBackendIF backend) {
    this.backend = backend;
  }

  public void loadSnapshot(SyncSource source) throws IOException, SAXException {
    log.info("Loading snapshot from " + source.getHandle());        
    SnapshotFeed feed = source.getSnapshotFeed();
    Snapshot snapshot = feed.getSnapshots().get(0);
    backend.loadSnapshot(this, snapshot);
  }
}

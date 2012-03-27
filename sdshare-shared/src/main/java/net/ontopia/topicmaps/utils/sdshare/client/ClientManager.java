
package net.ontopia.topicmaps.utils.sdshare.client;

import java.util.Collection;
import java.io.IOException;
import org.xml.sax.SAXException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the SyncThread. Has methods roughly corresponding to user
 * interface operations.
 */
public class ClientManager {
  private ClientConfig config;
  private String appname;
  private SyncThread thread;
  static Logger log = LoggerFactory.getLogger(ClientManager.class.getName());

  public ClientManager(ClientConfig config, String appname) {
    this.config = config;
    this.appname = appname;
    makeThread();
  }

  public ClientConfig getConfig() {
    return config;
  }

  public String getStatus() {
    return thread.getStatus();
  }

  public boolean isRunning() {
    return thread.isRunning();
  }

  public boolean isStopped() { // FIXME: do we need this?
    return !thread.isRunning();
  }
  
  public void startThread() {
    thread.start();
  }

  public void stopThread() {
    thread.stopThread();
    makeThread();
  }

  public void shutdown() {
    thread.stopThread();
    // the sync() method stops the main loop, and takes care of saving etc
  }

  public void loadSnapshots() throws IOException, SAXException {
    thread.loadSnapshots();
  }
  
  public void sync() throws IOException, SAXException {
    thread.sync(true);
  }

  public SyncSource getSource(String key) {
    return thread.getSource(key);
  }

  private void makeThread() {
    thread = new SyncThread(config.getBackend(), config.getEndpoints(),
                            appname);
  }
}

package net.ontopia.topicmaps.utils.sdshare.client;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import org.xml.sax.SAXException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ontopia.utils.StringUtils;
  
/**
 * INTERNAL: The thread that actually performs the sync operations.
 * Note that loadSnapshots() and sync() can be called both from run()
 * (that is, from within the thread), and from ClientManager in
 * response to UI events (in which case we're outside the thread).
 */
class SyncThread extends Thread {
  private String appname; // unique name of app, used to name state file
  private boolean stopped;
  private boolean running;
  private ClientBackendIF backend;
  private Collection<SyncEndpoint> endpoints;
  private Map<String, SyncSource> map;
  static Logger log = LoggerFactory.getLogger(SyncThread.class.getName());

  public SyncThread(ClientBackendIF backend,
                    Collection<SyncEndpoint> endpoints,
                    String appname) {
    this.backend = backend;
    this.endpoints = endpoints;
    this.appname = appname;

    // build a map of the sources for lookup purposes
    this.map = new HashMap();
    for (SyncEndpoint endpoint : endpoints)
      for (SyncSource source : endpoint.getSources())
        map.put(endpoint.getHandle() + " " + source.getHandle(), source);

    // load client state
    load();
  }
  
  public String getStatus() {
    if (running) {
      if (stopped)
        return "Waiting for thread to stop";
      else
        return "Running";
    } else {
      return "Not running .";
    }
  }
  
  public boolean isStopped() {
    return stopped;
  }

  public boolean isRunning() {
    return running;
  }

  public void stopThread() {
    stopped = true;
  }
    
  public void run() {
    stopped = false;
    running = true;
      
    while (!stopped) {
      try {
        sync(false);
      } catch (IOException e) {
        // this should only be IOExceptions from saving state, and so logging
        // and carrying on should be fine
        log.warn("Exception while syncing", e);
      }
      
      try {
        Thread.sleep(100); // wait 0.1 second, then check again
      } catch (InterruptedException e) {
        // this exception is really annoying
      }
    }
    
    running = false;
    stopped = false;
  }

  public SyncSource getSource(String key) {
    return map.get(key);
  }

  // --- THE ACTUAL OPERATIONS

  public void loadSnapshots() throws IOException, SAXException {
    for (SyncEndpoint endpoint : endpoints) 
      loadSnapshot(endpoint);
  }

  public void loadSnapshot(SyncEndpoint endpoint)
    throws IOException, SAXException {
    ClientBackendIF thebackend = endpoint.getBackend();
    if (thebackend == null)
      thebackend = backend;
    
    log.info("Getting snapshots for " + endpoint.getHandle());
    for (SyncSource source : endpoint.getSources()) {
      Snapshot snapshot = endpoint.loadSnapshot(source);
      backend.loadSnapshot(endpoint, snapshot);
    }
  }

  /**
   * Syncs all sources into their endpoints if it is time to check and
   * they are not blocked by errors.
   * @param force If true we ignore whether it's time to check yet.
   */
  public void sync(boolean force) throws IOException {
    boolean found = false;
    for (SyncEndpoint endpoint : endpoints) {
      log.trace("Checking endpoint " + endpoint.getHandle());
      ClientBackendIF thebackend = endpoint.getBackend();
      if (thebackend == null)
        thebackend = backend;
      
      for (SyncSource source : endpoint.getSources()) {
        // check that we haven't been stopped by the UI
        if (stopped)
          break;
        // verify that it's time to check this source now (this takes errors
        // into account, and delays checking correspondingly)
        if (!source.isTimeToCheck() && !force)
          continue;

        source.setActive(true);
        log.debug("Checking source " + source.getHandle() + " in " +
                  endpoint.getHandle());

        // it was time, so we download the feed and go through the
        // actual fragments
        try {
          Iterator<FragmentFeed> it = source.getFragmentFeeds();
          while (it.hasNext()) {
            FragmentFeed feed = it.next();
            log.debug("FOUND " + feed.getFragments().size() + " fragments");

            if (!feed.getFragments().isEmpty()) {
              thebackend.applyFragments(endpoint, feed.getFragments());
              for (Fragment fragment : feed.getFragments()) {
                source.setLastChange(fragment.getUpdated());
                source.addFragmentCount(1);
              }
              found = true;
            }
          }

          // we didn't see any errors, so clear any recorded errors for this
          // source
          source.clearError();
        } catch (Throwable e) {
          // we log the error, and note it on the source. that delays further
          // updates from the source, until we are told that we can continue,
          // or the errors end.
          log.error("Source " + source.getHandle() + " failed", e);
          source.setError(e.toString());
        }

        source.setActive(false);
        // this notes the time of the last update time for this source,
        // even if it failed.
        source.updated();
      }
    }
    if (found)
      save();
  }

  /**
   * Saves information about the current state of the client so that
   * we can carry on from where we stopped after the server is
   * restarted.
   */ 
  private void save() throws IOException {
    // line-based text format. each line is:
    //   endpoint-handle source-handle lastchange
    File f = new File(System.getProperty("java.io.tmpdir"),
                      appname + "-state.txt");
    log.debug("Saving state to " + f);
    FileWriter out = new FileWriter(f);

    for (SyncEndpoint endpoint : endpoints)
      for (SyncSource source : endpoint.getSources())
        out.write(endpoint.getHandle() + " " +
                  source.getHandle() + " " +
                  source.getLastChange() + "\n");
    
    out.close();
  }

  /**
   * Loads the state of the various sources from the save file.
   */
  private void load() {
    try {
      File f = new File(System.getProperty("java.io.tmpdir"),
                        appname + "-state.txt");
      BufferedReader in = new BufferedReader(new FileReader(f));
      String line = in.readLine();
      while (line != null) {
        String[] row = StringUtils.split(line.trim());

        SyncSource source = getSource(row[0] + " " + row[1]);
        if (source != null) {
          long last = Long.parseLong(row[2]);
          source.setLastChange(last);
        }
        line = in.readLine();
      }
      in.close();
    } catch (IOException e) {
      log.warn("Couldn't load state of sources: " + e);
      // we carry on anyway, assuming that we don't need the state info.
      // the usual cause of this warning is that we don't have any state
      // yet.
    }
  }
}

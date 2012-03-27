
package net.ontopia.topicmaps.utils.sdshare.client;

import java.util.Iterator;
import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * INTERNAL: A front end from which one can get change and snapshot feeds.
 */
public interface ClientFrontendIF {

  public String getHandle();

  public SnapshotFeed getSnapshotFeed() throws IOException, SAXException;

  // because of paging there may be more than one
  public Iterator<FragmentFeed> getFragmentFeeds(long lastChange)
    throws IOException, SAXException;
  
}
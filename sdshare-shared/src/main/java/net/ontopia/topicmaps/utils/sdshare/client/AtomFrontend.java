
package net.ontopia.topicmaps.utils.sdshare.client;

import java.util.Iterator;
import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * INTERNAL: Frontend which gets snapshots and changes by reading
 * SDshare Atom feeds.
 */
public class AtomFrontend implements ClientFrontendIF {
  private String handle; // handle (URL|id) of source collection
  private CollectionFeed feed;

  public AtomFrontend(String handle) {
    this.handle = handle;
  }
  
  public String getHandle() {
    return handle;
  }

  public SnapshotFeed getSnapshotFeed() throws IOException, SAXException {
    String feedurl = getFeed().getSnapshotFeed();
    return FeedReaders.readSnapshotFeed(feedurl);
  }

  public Iterator<FragmentFeed> getFragmentFeeds(long lastChange)
    throws IOException, SAXException {
    String feedurl = getFeed().getFragmentFeed();
    return new FeedIterator(FeedReaders.readFragmentFeed(feedurl, lastChange));
  }

  private CollectionFeed getFeed() throws IOException, SAXException {
    if (feed == null)
      feed = FeedReaders.readCollectionFeed(handle);
    return feed;
  }

  // --- Iterator implementation
  // this exists so we can handle paging

  class FeedIterator implements Iterator<FragmentFeed> {
    private FragmentFeed feed;

    public FeedIterator(FragmentFeed feed) {
      this.feed = feed;
    }
    
    public boolean hasNext() {
      return feed != null;
    }
    
    public FragmentFeed next() {
      FragmentFeed next = feed;
      if (feed.getNextLink() == null)
        feed = null;
      else {
        try {
          feed = FeedReaders.readFragmentFeed(feed.getNextLink());
        } catch (SAXException e) {
          throw new RuntimeException(e);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      return next;
    }
    
    public void remove() {
      throw new UnsupportedOperationException();
    }
    
  }
}
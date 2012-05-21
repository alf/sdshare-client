
package net.ontopia.topicmaps.utils.sdshare.client;

import java.util.Set;
import java.sql.Timestamp;

/**
 * PUBLIC: Represents an entry in a snapshot feed.
 */
public class Snapshot {
  private Timestamp timestamp;
  private SnapshotFeed feed;
  private Set<AtomLink> links;

  public Snapshot(SnapshotFeed feed) {
    this.feed = feed;
  }

  public Set<AtomLink> getLinks() {
    return links;
  }

  public void setLinks(Set<AtomLink> links) {
    this.links = links;
  }

  public Timestamp getUpdated() {
    return timestamp;
  }

  public void setUpdated(Timestamp timestamp) {
    this.timestamp = timestamp;
  }

  public SnapshotFeed getFeed() {
    return feed;
  }
}

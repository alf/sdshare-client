
package net.ontopia.topicmaps.utils.sdshare.client;

import java.util.Set;
import java.util.Collections;
import java.sql.Timestamp;

/**
 * PUBLIC: Represents an individual fragment in a fragment feed.
 */
public class Fragment {
  private Set<String> topicSIs;
  private Set<String> topicIIs;
  private Set<String> topicSLs;
  private Set<AtomLink> links;
  private String content;
  private Timestamp updated;
  private FragmentFeed parent;

  public Fragment(Set<AtomLink> links, Timestamp updated, String content) {
    this.links = links;
    this.updated = updated;
    this.content = content;
  }

  public Set<AtomLink> getLinks() {
    return links;
  }

  public Set<String> getTopicSIs() {
    return set(topicSIs);
  }

  public Set<String> getTopicIIs() {
    return set(topicIIs);
  }

  public Set<String> getTopicSLs() {
    return set(topicSLs);
  }

  private static Set<String> set(Set<String> set) {
    if (set == null)
      return Collections.EMPTY_SET;
    else
      return set;
  }

  public void setTopicSIs(Set<String> topicSIs) {
    this.topicSIs = topicSIs;
  }

  public void setTopicIIs(Set<String> topicIIs) {
    this.topicIIs = topicIIs;
  }

  public void setTopicSLs(Set<String> topicSLs) {
    this.topicSLs = topicSLs;
  }
  
  public Timestamp getUpdated() {
    return updated;
  }

  // used for SDshare push
  public String getContent() {
    return content;
  }

  public void setFeed(FragmentFeed parent) {
    this.parent = parent;
  }
  
  public FragmentFeed getFeed() {
    return parent;
  }
}

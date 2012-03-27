
package net.ontopia.topicmaps.utils.sdshare.client.test;

import java.io.File;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import net.ontopia.test.AbstractOntopiaTestCase;
import net.ontopia.topicmaps.utils.sdshare.client.AtomLink;
import net.ontopia.topicmaps.utils.sdshare.client.Snapshot;
import net.ontopia.topicmaps.utils.sdshare.client.AtomFrontend;
import net.ontopia.topicmaps.utils.sdshare.client.FragmentFeed;
import net.ontopia.topicmaps.utils.sdshare.client.SnapshotFeed;

public class AtomFrontendTest extends AbstractOntopiaTestCase {
  
  public AtomFrontendTest(String name) {
    super(name);
  }

  public void testSimple() throws Exception {
    String file = resolveFileName("sdshare" + File.separator + "feeds" +
                                  File.separator + "collection-simple.xml");
    
    AtomFrontend frontend = new AtomFrontend(file);
    assertEquals("wrong handle", file, frontend.getHandle());

    Iterator<FragmentFeed> it = frontend.getFragmentFeeds(0);
    FragmentFeed feed = it.next();

    // this feed is already tested in FeedReadersTest.testFragmentFeed1.
    // just repeating a minimal amount of testing here.
    assertEquals("wrong number of fragments",
                 feed.getFragments().size(), 1);
    assertEquals("incorrect server prefix",
                 "file:/Users/larsga/data/topicmaps/beer.xtm",
                 feed.getPrefix());
    assertEquals("shouldn't have reference to next page",
                 null,
                 feed.getNextLink());

    assertTrue("should have only one page", !it.hasNext());

    // now for the snapshot feed
    SnapshotFeed sfeed = frontend.getSnapshotFeed();
    assertEquals("incorrect server prefix",
                 "http://psi.hafslund.no/sesam/duke/data",
                 sfeed.getPrefix());

    List<Snapshot> snapshots = sfeed.getSnapshots();
    assertEquals("wrong number of snapshots", 1, snapshots.size());

    Snapshot snapshot = snapshots.get(0);
    Set<AtomLink> links = snapshot.getLinks();
    assertEquals("wrong number of links", 1, links.size());

    AtomLink link = links.iterator().next();
    assertTrue("wrong uri", link.getUri().endsWith("some-snapshot.rdf"));
    assertEquals("wrong main type", "application",
                 link.getMIMEType().getMainType());
    assertEquals("wrong sub type", "rdf+xml",
                 link.getMIMEType().getSubType());
    assertEquals("wrong version", null,
                 link.getMIMEType().getVersion());
  }

  public void testPaged() throws Exception {
    String file = resolveFileName("sdshare" + File.separator + "feeds" +
                                  File.separator + "collection-paged.xml");
    
    AtomFrontend frontend = new AtomFrontend(file);
    assertEquals("wrong handle", file, frontend.getHandle());

    Iterator<FragmentFeed> it = frontend.getFragmentFeeds(0);

    // this feed is already tested in FeedReadersTest.  just repeating
    // a minimal amount of testing here.
    FragmentFeed feed = it.next();
    assertEquals("wrong number of fragments",
                 feed.getFragments().size(), 1);
    assertEquals("incorrect server prefix",
                 "file:/Users/larsga/data/topicmaps/beer.xtm",
                 feed.getPrefix());
    assertTrue("should have reference to next page",
               feed.getNextLink().endsWith("fragment-paged-2.xml"));

    // this feed is already tested in FeedReadersTest.  just repeating
    // a minimal amount of testing here.
    feed = it.next();
    assertEquals("wrong number of fragments",
                 feed.getFragments().size(), 1);
    assertEquals("incorrect server prefix",
                 "file:/Users/larsga/data/topicmaps/beer.xtm",
                 feed.getPrefix());
    assertEquals("should not have reference to next page",
                 null,
                 feed.getNextLink());

    assertTrue("should be no more pages", !it.hasNext());
  }
}
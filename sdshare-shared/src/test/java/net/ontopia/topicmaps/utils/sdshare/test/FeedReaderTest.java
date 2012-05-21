
package net.ontopia.topicmaps.utils.sdshare.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.Date;
import java.util.Iterator;
import java.sql.Timestamp;

import org.xml.sax.XMLReader;
import org.xml.sax.DTDHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;

import net.ontopia.test.AbstractOntopiaTestCase;
import net.ontopia.topicmaps.utils.sdshare.client.*;
import net.ontopia.xml.XMLReaderFactoryIF;

public class FeedReaderTest extends AbstractOntopiaTestCase {
  
  public FeedReaderTest(String name) {
    super(name);
  }

  // ===== UTILITIES

  public FragmentFeed readFragmentFeed(String file)
    throws IOException, SAXException {
    file = resolveFileName("sdshare" + File.separator + "feeds" +
                           File.separator + file);
    return FeedReaders.readFragmentFeed(file);
  }

  public FragmentFeed readPostFeed(String file)
    throws IOException, SAXException {
    file = resolveFileName("sdshare" + File.separator + "feeds" +
                           File.separator + file);
    return FeedReaders.readPostFeed(new FileReader(file));
  }

  public void doSinceTest(String baseurl, Timestamp thetime, String finalurl)
    throws IOException, SAXException {
    FakeXMLReader our = new FakeXMLReader();
    XMLReaderFactoryIF orig = FeedReaders.parserfactory;
    FeedReaders.parserfactory = our;

    FeedReaders.readFragmentFeed(baseurl, thetime);

    FeedReaders.parserfactory = orig;    
    assertEquals("wrong URI used to retrieve feed", finalurl, our.uri);
  }
  
  // ===== TESTS

  public void testEmptyFragmentFeed() throws Exception {
    FragmentFeed feed = readFragmentFeed("fragment-empty.xml");
    assertTrue("fragments found in empty feed", feed.getFragments().isEmpty());
    assertEquals("incorrect server prefix",
                 "file:/Users/larsga/data/topicmaps/beer.xtm",
                 feed.getPrefix());
    assertEquals("shouldn't have reference to next page",
                 null,
                 feed.getNextLink());
  }

  public void testEmptyFragmentFeed2() throws Exception {
    FragmentFeed feed = readFragmentFeed("fragment-empty-2.xml");
    assertTrue("fragments found in empty feed", feed.getFragments().isEmpty());
    assertEquals("incorrect server prefix",
                 "file:/Users/larsga/data/topicmaps/beer.xtm",
                 feed.getPrefix());
    assertEquals("shouldn't have reference to next page",
                 null,
                 feed.getNextLink());
  }

  public void testFragmentFeed1() throws Exception {
    FragmentFeed feed = readFragmentFeed("fragment-1.xml");
    assertEquals("wrong number of fragments",
                 feed.getFragments().size(), 1);
    assertEquals("incorrect server prefix",
                 "file:/Users/larsga/data/topicmaps/beer.xtm",
                 feed.getPrefix());
    assertEquals("shouldn't have reference to next page",
                 null,
                 feed.getNextLink());

    Fragment fragment = feed.getFragments().iterator().next();
    Set<AtomLink> links = fragment.getLinks();
    assertEquals("wrong number of links", links.size(), 2);

    Iterator<AtomLink> it = links.iterator();
    AtomLink rdflink = it.next();
    AtomLink xtmlink = it.next();

    if (!rdflink.getMIMEType().getType().equals("application/rdf+xml")) {
      AtomLink tmp = rdflink;
      rdflink = xtmlink;
      xtmlink = tmp;
    }

    MIMEType xtm = xtmlink.getMIMEType();
    assertEquals("wrong MIME type",
                 rdflink.getMIMEType().toString(),
                 "application/rdf+xml");
    assertEquals("wrong MIME type", xtm.toString(),
                 "application/x-tm+xml; version=1.0");
    assertTrue("wrong link: " + xtmlink.getUri(),
               xtmlink.getUri().endsWith("&syntax=xtm"));
    assertTrue("wrong link: " + rdflink.getUri(),
               rdflink.getUri().endsWith("&syntax=rdf"));

    assertEquals("wrong MIME main type", xtm.getMainType(), "application");
    assertEquals("wrong MIME subtype", xtm.getSubType(), "x-tm+xml");
    assertEquals("wrong MIME version", xtm.getVersion(), "1.0");
  }

  public void testFragmentFeedPrecise() throws Exception {
    FragmentFeed feed = readFragmentFeed("fragment-precise.xml");
    assertEquals("wrong number of fragments",
                 feed.getFragments().size(), 1);
    assertEquals("incorrect server prefix",
                 "file:/Users/larsga/data/topicmaps/beer.xtm",
                 feed.getPrefix());
    assertEquals("shouldn't have reference to next page",
                 null,
                 feed.getNextLink());

    Fragment fragment = feed.getFragments().iterator().next();
    Set<AtomLink> links = fragment.getLinks();
    assertEquals("wrong number of links", links.size(), 2);

    Iterator<AtomLink> it = links.iterator();
    AtomLink rdflink = it.next();
    AtomLink xtmlink = it.next();

    if (!rdflink.getMIMEType().getType().equals("application/rdf+xml")) {
      AtomLink tmp = rdflink;
      rdflink = xtmlink;
      xtmlink = tmp;
    }

    MIMEType xtm = xtmlink.getMIMEType();
    assertEquals("wrong MIME type",
                 rdflink.getMIMEType().toString(),
                 "application/rdf+xml");
    assertEquals("wrong MIME type", xtm.toString(),
                 "application/x-tm+xml; version=1.0");
    assertTrue("wrong link: " + xtmlink.getUri(),
               xtmlink.getUri().endsWith("&syntax=xtm"));
    assertTrue("wrong link: " + rdflink.getUri(),
               rdflink.getUri().endsWith("&syntax=rdf"));

    assertEquals("wrong MIME main type", xtm.getMainType(), "application");
    assertEquals("wrong MIME subtype", xtm.getSubType(), "x-tm+xml");
    assertEquals("wrong MIME version", xtm.getVersion(), "1.0");
  }  

  public void testFragmentFeedPaged() throws Exception {
    FragmentFeed feed = readFragmentFeed("fragment-paged.xml");
    assertEquals("wrong number of fragments",
                 feed.getFragments().size(), 1);
    assertEquals("incorrect server prefix",
                 "file:/Users/larsga/data/topicmaps/beer.xtm",
                 feed.getPrefix());
    assertTrue("incorrect reference to next page",
               feed.getNextLink().endsWith("fragment-paged-2.xml"));

    Fragment fragment = feed.getFragments().iterator().next();
    Set<AtomLink> links = fragment.getLinks();
    assertEquals("wrong number of links", links.size(), 2);

    Iterator<AtomLink> it = links.iterator();
    AtomLink rdflink = it.next();
    AtomLink xtmlink = it.next();

    if (!rdflink.getMIMEType().getType().equals("application/rdf+xml")) {
      AtomLink tmp = rdflink;
      rdflink = xtmlink;
      xtmlink = tmp;
    }

    MIMEType xtm = xtmlink.getMIMEType();
    assertEquals("wrong MIME type",
                 rdflink.getMIMEType().toString(),
                 "application/rdf+xml");
    assertEquals("wrong MIME type", xtm.toString(),
                 "application/x-tm+xml; version=1.0");
    assertTrue("wrong link: " + xtmlink.getUri(),
               xtmlink.getUri().endsWith("&syntax=xtm"));
    assertTrue("wrong link: " + rdflink.getUri(),
               rdflink.getUri().endsWith("&syntax=rdf"));

    assertEquals("wrong MIME main type", xtm.getMainType(), "application");
    assertEquals("wrong MIME subtype", xtm.getSubType(), "x-tm+xml");
    assertEquals("wrong MIME version", xtm.getVersion(), "1.0");
  }
  
  public void testPostFeed1() throws Exception {
    FragmentFeed feed = readPostFeed("push-1.xml");
    assertEquals("wrong number of fragments",
                 feed.getFragments().size(), 1);
    assertEquals("incorrect server prefix",
                 "file:/Users/larsga/data/topicmaps/beer.xtm",
                 feed.getPrefix());
    assertEquals("shouldn't have reference to next page",
                 null,
                 feed.getNextLink());

    Fragment fragment = feed.getFragments().iterator().next();
    Set<AtomLink> links = fragment.getLinks();
    assertEquals("wrong number of links", links.size(), 0);

    assertTrue("no content in fragment", fragment.getContent() != null);

    // FIXME: should test contents of fragment, too
    // FIXME: should test updated-times, too
  }

  public void testFragmentSince() throws Exception {
    String timestring = "2011-03-24T10:04:02Z";
    Timestamp thetime = FeedReaders.parseDateTime(timestring);
    String baseurl = "http://www.example.org/sdshare/fragments";
    doSinceTest(baseurl, thetime, baseurl + "?since=" + timestring);
  }

  public void testFragmentSinceFile() throws Exception {
    String timestring = "2011-03-24T10:04:02Z";
    Timestamp thetime = FeedReaders.parseDateTime(timestring);
    String baseurl = "file://Users/larsga/sdshare/fragments.xml";
    doSinceTest(baseurl, thetime, baseurl);
  }

  public void testFragmentSinceNoTime() throws Exception {
    String baseurl = "http://www.example.org/sdshare/fragments";
    doSinceTest(baseurl, null, baseurl);
  }

  public void testFragmentSinceParamsAlready() throws Exception {
    String timestring = "2011-03-24T10:04:02Z";
    Timestamp thetime = FeedReaders.parseDateTime(timestring);
    String baseurl = "http://www.example.org/sdshare/fragments?tm=x.xtm";
    doSinceTest(baseurl, thetime, baseurl + "&since=" + timestring);
  }

  // ===== DATE/TIME TESTING

  // "2002-05-22T22:13:21Z" -> 1022098401.000
  // "2002-05-22T22:13:21.380Z" -> 1022098401.380
  // "2002-05-22T22:13:21.380123456Z" -> 1022098401.380123456
  
  public void testTimeParser() {
    Timestamp correct = new Timestamp(1022098401000L);
    Timestamp parsed = FeedReaders.parseDateTime("2002-05-22T22:13:21Z");
    assertEquals(correct, parsed);
  }

  public void testTimeParser2() {
    Timestamp correct = new Timestamp(1022098401380L);
    Timestamp parsed = FeedReaders.parseDateTime("2002-05-22T22:13:21.380Z");
    assertEquals(correct, parsed);
  }

  public void testTimeParser3() {
    Timestamp correct = new Timestamp(1022098401000L);
    correct.setNanos(380123456);
    Timestamp parsed = FeedReaders.parseDateTime("2002-05-22T22:13:21.380123456Z");
    assertEquals(correct, parsed);
  }
  
  public void testTimeFormatter() {
    Timestamp origin = new Timestamp(1022098401000L);
    assertEquals("2002-05-22T22:13:21Z", FeedReaders.format(origin));
  }

  public void testTimeFormatter2() {
    Timestamp origin = new Timestamp(1022098401380L);
    assertEquals("2002-05-22T22:13:21.38Z", FeedReaders.format(origin));
  }

  public void testTimeFormatter3() {
    Timestamp origin = new Timestamp(1022098401000L);
    origin.setNanos(380123456);
    assertEquals("2002-05-22T22:13:21.380123456Z", FeedReaders.format(origin));
  }
  
  // ===== FAKE XML READER =====

  /**
   * This class exists only so we can pick up the URI passed to the parser.
   */
  class FakeXMLReader implements XMLReader, XMLReaderFactoryIF {
    private String uri;

    // XMLReader implementation
    
    public ContentHandler getContentHandler() { return null; }
    public void setContentHandler(ContentHandler c) {}
    public DTDHandler getDTDHandler() { return null; }
    public void setDTDHandler(DTDHandler d) {}
    public EntityResolver getEntityResolver() { return null; }
    public void setEntityResolver(EntityResolver e) {}
    public ErrorHandler getErrorHandler() { return null; }
    public void setErrorHandler(ErrorHandler e) {}
    public boolean getFeature(String name) { return false; }
    public void setFeature(String name, boolean v) {}
    public Object getProperty(String name) { return null; }
    public void setProperty(String name, Object v) {}
    public void parse(InputSource src) {}
    
    public void parse(String uri) {
      this.uri = uri;
    }

    // XMLReaderFactoryIF implementation

    public XMLReader createXMLReader() {
      return this;
    }
  }
}
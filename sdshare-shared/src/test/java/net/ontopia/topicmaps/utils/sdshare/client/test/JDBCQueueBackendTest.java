
package net.ontopia.topicmaps.utils.sdshare.client.test;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Collections;
import java.sql.Driver;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;

import net.ontopia.test.AbstractOntopiaTestCase;
import net.ontopia.topicmaps.utils.sdshare.client.*;

public class JDBCQueueBackendTest extends AbstractOntopiaTestCase {
  private SyncEndpoint endpoint;
  private JDBCQueueBackend backend;
  private Statement stmt;
  
  public JDBCQueueBackendTest(String name) {
    super(name);
  }

  public void setUp() throws SQLException {
    endpoint = new SyncEndpoint("jdbc:h2:mem");
    endpoint.setProperty("driver-class", "org.h2.Driver");
    endpoint.setProperty("database", "h2");

    backend = new JDBCQueueBackend();

    stmt = getConnection(endpoint);
    stmt.executeUpdate("delete from UPDATED_RESOURCES"); // empty DB
  }

  // FIXME: can't test snapshot, because it doesn't work...

  public void testEmpty() throws SQLException {
    backend.applyFragments(endpoint, new ArrayList<Fragment>());

    // database should now be there, but be empty

    ResultSet rs = stmt.executeQuery("select * from UPDATED_RESOURCES");
    boolean present = rs.next();
    rs.close();

    assertTrue("database should be empty", !present);
  }

  public void testOneFragment() throws SQLException {
    final String PSI = "http://psi.example.org";
    final String LINK = "http://example.org/the-link";
    
    List<Fragment> fragments = new ArrayList<Fragment>();
    AtomLink l = new AtomLink(new MIMEType("application/rdf+xml"), LINK);
    Fragment f = new Fragment(Collections.singleton(l), 0, null);
    f.setTopicSIs(Collections.singleton(PSI));
    fragments.add(f);
    backend.applyFragments(endpoint, fragments);

    ResultSet rs = stmt.executeQuery("select * from UPDATED_RESOURCES");
    assertTrue("database should not be empty", rs.next());
    assertEquals("bad PSI", rs.getString("URI"), PSI);
    assertEquals("bad link", rs.getString("FRAGMENT_URI"), LINK);
    rs.close();
  }

  public void testChooseRightSyntax() throws SQLException {
    final String PSI = "http://psi.example.org";
    final String LINK1 = "http://example.org/the-link";
    final String LINK2 = "http://example.org/bad-link";
    
    List<Fragment> fragments = new ArrayList<Fragment>();
    Set<AtomLink> links = new HashSet();
    links.add(new AtomLink(new MIMEType("application/rdf+xml"), LINK1));
    links.add(new AtomLink(new MIMEType("text/xtm"), LINK2));
    Fragment f = new Fragment(links, 0, null);
    f.setTopicSIs(Collections.singleton(PSI));
    fragments.add(f);
    backend.applyFragments(endpoint, fragments);

    ResultSet rs = stmt.executeQuery("select * from UPDATED_RESOURCES");
    assertTrue("database should not be empty", rs.next());
    assertEquals("bad PSI", rs.getString("URI"), PSI);
    assertEquals("bad link", rs.getString("FRAGMENT_URI"), LINK1);
    rs.close();
  }
  
  // --- INTERNAL HELPERS

  // this is a cut-down copy of the method in JDBCQueueBackend
  private Statement getConnection(SyncEndpoint endpoint) {
    String jdbcuri = endpoint.getHandle();
    String driverklass = endpoint.getProperty("driver-class");
    
    try {
      Class driverclass = Class.forName(driverklass);
      Driver driver = (Driver) driverclass.newInstance();
      Properties props = new Properties();
      Connection conn = driver.connect(jdbcuri, props);
      return conn.createStatement();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
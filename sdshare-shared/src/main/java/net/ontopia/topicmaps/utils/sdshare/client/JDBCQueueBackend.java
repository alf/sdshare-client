
package net.ontopia.topicmaps.utils.sdshare.client;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.io.IOException;
import java.sql.Driver;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;

import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.StatementHandler;

import net.ontopia.utils.SDShareRuntimeException;
import net.ontopia.utils.StringUtils;
import net.ontopia.topicmaps.utils.rdf.RDFUtils;

/**
 * INTERNAL: Backend which stores list of changed URIs in a database
 * via JDBC so that another process can get them from there.
 *
 * <p>The handle is the JDBC URI. Configuration properties:
 * driver-class, username, password, and database (ie: which kind of
 * database). Only H2 and Oracle supported for now.
 */
public class JDBCQueueBackend extends AbstractBackend
  implements ClientBackendIF {
  //static Logger log = LoggerFactory.getLogger(JDBCQueueBackend.class.getName());
  private static Collection<String> ignore_uri_prefixes;
  private String prevsi; // primitive caching
  
  public void loadSnapshot(SyncEndpoint endpoint, Snapshot snapshot) {
    if (endpoint.getProperty("ignore-uri-prefixes") != null)
      setIgnoreUriPrefixes(endpoint.getProperty("ignore-uri-prefixes"));

    InsertHandler handler = new InsertHandler(endpoint);
    try {
      // FIXME: should we delete contents first?
      String sourceuri = findPreferredLink(snapshot.getLinks()).getUri();
      RDFUtils.parseRDFXML(sourceuri, handler);
      handler.close();
    } catch (IOException e) {
      throw new SDShareRuntimeException(e);
    }
  }

  public void applyFragments(SyncEndpoint endpoint, List<Fragment> fragments) {
    String tblprefix = "";
    if (endpoint.getProperty("table-prefix") != null)
      tblprefix = endpoint.getProperty("table-prefix");
    if (endpoint.getProperty("ignore-uri-prefixes") != null)
      setIgnoreUriPrefixes(endpoint.getProperty("ignore-uri-prefixes"));
    
    DatabaseType dbtype = getDBType(endpoint);
    Statement stmt = getConnection(endpoint);
    try {
      try {
        for (Fragment f : fragments) {
          // FIXME: this is getting ugly. too many parameters.
          String psi = f.getTopicSIs().iterator().next();
          writeResource(stmt, psi, findPreferredLink(f.getLinks()).getUri(),
                        dbtype, tblprefix);
        }
        stmt.getConnection().commit();
      } finally {
        stmt.close();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeResource(Statement stmt, String topicsi, String datauri,
                             DatabaseType dbtype, String tblprefix)
    throws SQLException {
    // check if we already saw this topicsi in previous statement
    if (prevsi != null && prevsi.equals(topicsi))
      return;
    prevsi = topicsi;
    
    // check if this is a URL pattern we don't want to queue
    if (ignore_uri_prefixes != null)
      for (String prefix : ignore_uri_prefixes)
        if (topicsi.startsWith(prefix))
          return;

    // check if the URL is already there
    ResultSet rs = stmt.executeQuery("select id from " + tblprefix +
                                     "UPDATED_RESOURCES where URI = '" +
                                     escape(topicsi) + "'");
    boolean present = rs.next();
    rs.close();
    if (present)
      return; // it's already there, so we don't need to do anything
    
    // ok, carry on
    String idvalue;
    if (dbtype == DatabaseType.H2)
      idvalue = "NULL";
    else
      idvalue = tblprefix + "resource_seq.nextval";

    String escaped_data_uri = "NULL";
    if (datauri != null)
      escaped_data_uri = "'" + escape(datauri) + "'";
    stmt.executeUpdate("insert into " + tblprefix + "UPDATED_RESOURCES " +
                       "values (" + idvalue + ", '" + escape(topicsi) + "', " +
                       escaped_data_uri + ")");
  }

  public int getLinkScore(AtomLink link) {
    MIMEType mimetype = link.getMIMEType();
    // FIXME: this is too simplistic. we could probably support more
    // syntaxes than just this one, but for now this will have to do.
    if (mimetype.getType().equals("application/rdf+xml"))
      return 100;
    return 0;
  }

  private Statement getConnection(SyncEndpoint endpoint) {
    String jdbcuri = endpoint.getHandle();
    String driverklass = endpoint.getProperty("driver-class");
    if (driverklass == null)
      throw new RuntimeException("Endpoint property driver-class not set on " +
                                 "endpoint " + jdbcuri);
    String username = endpoint.getProperty("username");
    String password = endpoint.getProperty("password");
    
    try {
      Class driverclass = Class.forName(driverklass);
      Driver driver = (Driver) driverclass.newInstance();
      Properties props = new Properties();
      if (username != null)
        props.put("user", username);
      if (password != null)
        props.put("password", password);
      Connection conn = driver.connect(jdbcuri, props);
      Statement stmt = conn.createStatement();
      
      // check that tables exist & create if not
      verifySchema(stmt, getDBType(endpoint));
      
      return stmt;
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

  private void verifySchema(Statement stmt, DatabaseType dbtype)
    throws SQLException {
    String tablename;
    if (dbtype == DatabaseType.H2)
      tablename = "information_schema.tables";
    else if (dbtype == DatabaseType.ORACLE)
      tablename = "all_tables";
    else
      throw new SDShareRuntimeException("Unknown database type: " + dbtype);
    
    ResultSet rs = stmt.executeQuery("select * from " + tablename + " where " +
                                     "table_name = 'UPDATED_RESOURCES'");
    boolean present = rs.next();
    rs.close();

    if (present)
      return;

    if (dbtype == DatabaseType.H2) {
      stmt.executeUpdate("create table UPDATED_RESOURCES ( " +
                         "  id int auto_increment primary key, " +
                         "  uri varchar not null, " +
                         "  fragment_uri varchar )");

      stmt.executeUpdate("create index URI_IX on UPDATED_RESOURCES (uri)");
    } else if (dbtype == DatabaseType.ORACLE) {
      // first we must create a sequence, so we can get autoincrement
      stmt.executeUpdate("create sequence resource_seq " +
                         "start with 1 increment by 1 nomaxvalue");

      // then we can create the table
      stmt.executeUpdate("create table UPDATED_RESOURCES ( " +
                         "  id int not null, " +
                         "  uri varchar(200) not null, " +
                         "  fragment_uri(400) varchar, " +
                         "CONSTRAINT updated_pk PRIMARY KEY (id))");

      stmt.executeUpdate("create index URI_IX on UPDATED_RESOURCES (uri)");
    }
  }

  private String escape(String strval) {
    return strval.replace("'", "''");
  }

  private void setIgnoreUriPrefixes(String ignores) {
    ignore_uri_prefixes = new ArrayList<String>();
    String[] tokens = StringUtils.split(ignores);
    for (int ix = 0; ix < tokens.length; ix++)
      ignore_uri_prefixes.add(tokens[ix]);
  }

  // ===== Writing INSERT-format triples

  public class InsertHandler implements StatementHandler {
    private Statement stmt;
    private DatabaseType dbtype;
    private String tblprefix;
    
    public InsertHandler(SyncEndpoint endpoint) {
      this.stmt = getConnection(endpoint);
      this.dbtype = getDBType(endpoint);

      // FIXME: next three lines are duplicated
      this.tblprefix = "";
      if (endpoint.getProperty("table-prefix") != null)
        this.tblprefix = endpoint.getProperty("table-prefix");
    }

    public void statement(AResource sub, AResource pred, ALiteral lit) {
      try {
        // FIXME: this doesn't handle blank nodes
        writeResource(stmt, sub.getURI(), null, dbtype, tblprefix);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    public void statement(AResource sub, AResource pred, AResource obj) {
      try {
        // FIXME: this doesn't handle blank nodes
        writeResource(stmt, sub.getURI(), null, dbtype, tblprefix);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
     
    public void close() {
      try {
        stmt.getConnection().commit();
        stmt.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }
 
  // ===== ENUM FOR DATABASE TYPE

  private static DatabaseType getDBType(SyncEndpoint endpoint) {
    return getDBType(endpoint.getProperty("database"));
  }
  
  private static DatabaseType getDBType(String dbtype) {
    if (dbtype == null)
      throw new SDShareRuntimeException("duke.database property not set");
    else if (dbtype.equals("h2"))
      return DatabaseType.H2;
    else if (dbtype.equals("oracle"))
      return DatabaseType.ORACLE;
    else
      throw new SDShareRuntimeException("Unknown database type: " + dbtype);
  }
  
  public enum DatabaseType {
    H2, ORACLE
  }
}
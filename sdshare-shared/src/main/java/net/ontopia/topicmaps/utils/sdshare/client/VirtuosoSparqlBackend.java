
package net.ontopia.topicmaps.utils.sdshare.client;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Date;
import java.io.Writer;
import java.io.IOException;
import java.io.StringWriter;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.StatementHandler;

import net.ontopia.utils.SDShareRuntimeException;
import net.ontopia.utils.StreamUtils;
import net.ontopia.topicmaps.utils.rdf.RDFUtils;

/**
 * INTERNAL: A subclass of the standard SparqlBackend which implements
 * the Virtuoso dialect of SPARQL Update. This will only work for
 * certain Virtuoso versions.
 */
public class VirtuosoSparqlBackend extends SparqlBackend {

  protected String makeDeleteStatement(String graph, String subject) {
    // http://sourceforge.net/mailarchive/forum.php?thread_name=7DF4AA0F-2DAD-4A58-B3D5-1081CA05D94A%40openlinksw.com&forum_name=virtuoso-users
    return "DEFINE  sql:log-enable 2 " +
           "delete from <" + graph + "> " +
           "  { <" + subject + "> ?p ?v } " +
           "where " +
           "  { <" + subject + "> ?p ?v }";
  }

  protected String getParameterName() {
    return "query";
  }

  protected String makeInsertStatement(String graph, String stmts) {
    // http://sourceforge.net/mailarchive/forum.php?thread_name=7DF4AA0F-2DAD-4A58-B3D5-1081CA05D94A%40openlinksw.com&forum_name=virtuoso-users
    return "DEFINE  sql:log-enable 2 " +
           "insert data into <" + graph + "> { " + stmts + " }";
   }
}
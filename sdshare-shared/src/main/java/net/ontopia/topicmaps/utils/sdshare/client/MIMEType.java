
package net.ontopia.topicmaps.utils.sdshare.client;

/**
 * INTERNAL: Represents a MIME type.
 */
public class MIMEType {
  private String maintype;
  private String subtype;
  private String version; // can be null

  public MIMEType(String maintype, String subtype, String version) {
    this.maintype = maintype;
    this.subtype = subtype;
    this.version = version;
  }

  /**
   * INTERNAL: Parses the MIME type string representation.
   */
  public MIMEType(String mimetype) {
    int slash = mimetype.indexOf('/');
    maintype = mimetype.substring(0, slash);

    int semicolon = mimetype.indexOf(';');
    if (semicolon == -1)
      subtype = mimetype.substring(slash + 1);
    else {
      subtype = mimetype.substring(slash + 1, semicolon);

      String lastpart = mimetype.substring(semicolon);
      int ix = eatWS(1, lastpart);
      if (ix >= lastpart.length() ||
          !lastpart.substring(ix, ix + 7).equals("version"))
        return;

      ix = eatWS(ix + 7, lastpart);
      if (ix >= lastpart.length() ||
          lastpart.charAt(ix) != '=')
        return;
      
      ix = eatWS(ix + 1, lastpart);
      version = lastpart.substring(ix).trim();
    }
  }
  
  private int eatWS(int ix, String str) {
    while (ix < str.length() && str.charAt(ix) == ' ')
      ix++;
    return ix;
  }

  public String getMainType() {
    return maintype;
  }

  public String getSubType() {
    return subtype;
  }

  /**
   * Returns the entire MIME type, minus any parameters. So for
   * <tt>'application/x-tm+xml; version=1.0'</tt> this would be
   * <tt>'application/x-tm+xml'</tt>.
   */
  public String getType() {
    return maintype + "/" + subtype;
  }

  public String getVersion() {
    return version;
  }

  public String toString() {
    if (version == null)
      return getType();
    else
      return getType() + "; version=" + version;
  }
}
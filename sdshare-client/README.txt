
  EXPERIMENTAL SDSHARE IMPLEMENTATION
=======================================

This is an experimental implementation of the SDshare protocol, as
defined here:
  http://www.egovpt.org/fg/CWA_Part_1b

It has not been properly tested yet, so please do not rely on this for
anything serious.


--- WHAT IT DOES

This is an SDshare client. It's run as a web application, and can pull
from any number of sources, and push to any number of recipients. The
main sources are SDshare feeds, and the main recipients are SPARQL
Update endpoints. However, frontends and backends are pluggable.


--- INSTALLATION

Run "mvn package", then copy target/sdshare-client.war into your
apache-tomcat/webapps directory, probably under the name
"sdshare-client".

[need to remember how to configure it]

Start the server.

Go to http://localhost:8080/sdshare-client/



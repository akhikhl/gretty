package gretty

import org.eclipse.jetty.security.HashLoginService
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.webapp.WebAppContext

class GrettyHelper {

  Server createServer() {
    return new Server()
  }

  void createConnectors(Server jettyServer, int port) {
    SocketConnector connector = new SocketConnector()
    // Set some timeout options to make debugging easier.
    connector.setMaxIdleTime(1000 * 60 * 60)
    connector.setSoLingerTime(-1)
    connector.setPort(port)
    jettyServer.setConnectors([connector ] as Connector[])
  }

  WebAppContext createWebAppContext() {
    return new WebAppContext()
  }

  void setRealm(WebAppContext context, String realmName, String realmConfigFile) {
    context.getSecurityHandler().setLoginService(new HashLoginService(realmName, realmConfigFile))
  }
}

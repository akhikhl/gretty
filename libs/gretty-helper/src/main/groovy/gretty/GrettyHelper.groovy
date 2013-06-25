package gretty

import org.eclipse.jetty.security.HashLoginService
import org.eclipse.jetty.security.LoginService
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.webapp.WebAppContext

class GrettyHelper {

  public static Connector[] createConnectors(int port) {
    SocketConnector connector = new SocketConnector()
    // Set some timeout options to make debugging easier.
    connector.setMaxIdleTime(1000 * 60 * 60)
    connector.setSoLingerTime(-1)
    connector.setPort(port)
    return [connector ] as Connector[]
  }

  public static LoginService createLoginService(String realmName, String realmConfigFile) {
    return new HashLoginService(realmName, realmConfigFile)
  }

  public static Server createServer() {
    return new Server()
  }

  public static WebAppContext createWebAppContext() {
    return new WebAppContext()
  }
}

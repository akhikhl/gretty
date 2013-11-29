/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.eclipse.jetty.security.HashLoginService
import org.eclipse.jetty.security.LoginService
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.webapp.WebAppClassLoader
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.util.Scanner
import org.eclipse.jetty.util.Scanner.BulkListener
import org.eclipse.jetty.util.Scanner.ScanCycleListener

class GrettyHelper {

  public static addScannerBulkListener(Scanner scanner, listener) {
    scanner.addListener(new BulkListener() {
      void filesChanged(List<String> filenames) {
        listener.call(filenames)
      }
    });
  }

  public static addScannerScanCycleListener(Scanner scanner, listener) {
    scanner.addListener(new ScanCycleListener() {
      void scanEnded(int cycle) {
        listener.call(false, cycle)
      }
      void scanStarted(int cycle) {
        listener.call(true, cycle)
      }
    });
  }

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

  public static Scanner createScanner() {
    return new Scanner()
  }

  public static Server createServer() {
    return new Server()
  }

  public static void setClassLoader(WebAppContext context, URLClassLoader classLoader) {
    context.setClassLoader(new WebAppClassLoader(classLoader, context))
  }

  public static WebAppContext createWebAppContext() {
    return new WebAppContext()
  }
}

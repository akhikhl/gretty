/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file 'license.txt' for copying and usage permission.
 */
package org.akhikhl.gretty

import org.apache.catalina.Context
import org.apache.catalina.connector.Connector
import org.apache.catalina.startup.Tomcat
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class TomcatServerStartInfo {

  protected final Logger log

  TomcatServerStartInfo() {
    log = LoggerFactory.getLogger(this.getClass())
  }

  def getInfo(Tomcat tomcat, Connector[] connectors, Map params) {

    if(connectors == null)
      connectors = tomcat.service.findConnectors()

    def portsInfo = connectors.collect { it.port }
    portsInfo = (portsInfo.size() == 1 ? 'port ' : 'ports ') + portsInfo.join(', ')
    log.warn '{} started and listening on {}', params.servletContainerDescription, portsInfo

    Connector httpConn = connectors.find { it.scheme == 'http' }
    Connector httpsConn = connectors.find { it.scheme == 'https' }

    List contextInfo = []

    for(Context context in tomcat.host.findChildren().findAll { it instanceof Context }) {
      log.warn '{} runs at:', (context.name - '/')
      if(httpConn) {
        log.warn '  http://{}:{}{}', (tomcat.hostname == '0.0.0.0' ? 'localhost' : tomcat.hostname), httpConn.port, context.path
        httpConn.with {
          contextInfo.add([ protocol: 'http', host: tomcat.hostname, port: port, contextPath: context.path, baseURI: "http://${tomcat.hostname}:${port}${context.path}" ])
        }
      }
      if(httpsConn) {
        log.warn '  https://{}:{}{}', (tomcat.hostname == '0.0.0.0' ? 'localhost' : tomcat.hostname), httpsConn.port, context.path
        httpsConn.with {
          contextInfo.add([ protocol: 'https', host: tomcat.hostname, port: port, contextPath: context.path, baseURI: "https://${tomcat.hostname}:${port}${context.path}" ])
        }
      }
    }

    def serverStartInfo = [ status: 'successfully started' ]

    serverStartInfo.host = tomcat.hostname

    if(httpConn)
      serverStartInfo.httpPort = httpConn.port

    if(httpsConn)
      serverStartInfo.httpsPort = httpsConn.port

    serverStartInfo.contexts = contextInfo
    serverStartInfo
  }
}


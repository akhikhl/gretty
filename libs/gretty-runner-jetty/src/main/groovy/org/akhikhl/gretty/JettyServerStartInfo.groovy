/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file 'license.txt' for copying and usage permission.
 */
package org.akhikhl.gretty

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class JettyServerStartInfo {

  protected final Logger log

  JettyServerStartInfo() {
    log = LoggerFactory.getLogger(this.getClass())
  }

  def getInfo(server, JettyConfigurer configurer, Map params) {

    def portsInfo = server.getConnectors().collect { it.port }
    portsInfo = (portsInfo.size() == 1 ? 'port ' : 'ports ') + portsInfo.join(', ')
    log.warn '{} started and listening on {}', params.servletContainerDescription, portsInfo

    def httpConn = configurer.findHttpConnector(server)
    def httpsConn = configurer.findHttpsConnector(server)

    List contextInfo = []

    for(def context in server.handler.handlers) {
      log.warn '{} runs at:', (context.displayName - '/')
      if(httpConn) {
        log.warn '  http://{}:{}{}', (httpConn.host == '0.0.0.0' ? 'localhost' : httpConn.host), httpConn.port, context.contextPath
        httpConn.with {
          contextInfo.add([ protocol: 'http', host: host, port: port, contextPath: context.contextPath, baseURI: "http://${host}:${port}${context.contextPath}" ])
        }
      }
      if(httpsConn) {
        log.warn '  https://{}:{}{}', (httpsConn.host == '0.0.0.0' ? 'localhost' : httpsConn.host), httpsConn.port, context.contextPath
        httpsConn.with {
          contextInfo.add([ protocol: 'https', host: host, port: port, contextPath: context.contextPath, baseURI: "https://${host}:${port}${context.contextPath}" ])
        }
      }
    }

    def serverStartInfo = [ status: 'successfully started' ]

    serverStartInfo.host = httpConn?.host ?: httpsConn?.host

    if(httpConn)
      serverStartInfo.httpPort = httpConn.port

    if(httpsConn)
      serverStartInfo.httpsPort = httpsConn.port

    serverStartInfo.contexts = contextInfo
    serverStartInfo
  }
}


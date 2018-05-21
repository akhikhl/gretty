/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.apache.catalina.Context
import org.apache.catalina.connector.Connector
import org.apache.catalina.startup.Tomcat
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
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
    log.info '{} started and listening on {}', params.servletContainerDescription, portsInfo

    Connector httpConn = connectors.find { it.scheme == 'http' }
    Connector httpsConn = connectors.find { it.scheme == 'https' }

    List contextInfo = []

    String host = tomcat.hostname == '0.0.0.0' ? 'localhost' : tomcat.hostname

    for(Context context in tomcat.host.findChildren().findAll { it instanceof Context }) {
      log.info '{} runs at:', (context.name - '/')
      if(httpConn) {
        log.info '  http://{}:{}{}', host, httpConn.port, context.path
        contextInfo.add([
          protocol: 'http',
          host: host,
          port: httpConn.port,
          contextPath: context.path,
          baseURI: "http://${host}:${httpConn.port}${context.path}"
        ])
      }
      if(httpsConn) {
        log.info '  https://{}:{}{}', host, httpsConn.port, context.path
        contextInfo.add([
          protocol: 'https',
          host: host,
          port: httpsConn.port,
          contextPath: context.path,
          baseURI: "https://${host}:${httpsConn.port}${context.path}"
        ])
      }
    }

    def serverStartInfo = [ status: 'successfully started' ]

    serverStartInfo.host = host

    if(httpConn)
      serverStartInfo.httpPort = httpConn.port

    if(httpsConn)
      serverStartInfo.httpsPort = httpsConn.port

    serverStartInfo.contexts = contextInfo
    serverStartInfo
  }
}


/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
final class JettyServerManager implements ServerManager {

  private JettyConfigurer configurer
  protected Map params
	protected server
  protected Logger log

  JettyServerManager(JettyConfigurer configurer) {
    this.configurer = configurer
  }

  @Override
  void setParams(Map params) {
    this.params = params
  }

  @Override
  void startServer() {
    assert server == null

    if(params.logging)
      LoggingUtils.configureLogging(params.logging)
    else if(params.logbackConfig)
      LoggingUtils.useConfig(params.logbackConfig)

    log = LoggerFactory.getLogger(this.getClass())
    configurer.setLogger(log)

    server = new JettyServerConfigurer().createAndConfigureServer(configurer, params)
    server.start()

    def connectors = server.getConnectors()

    def portsInfo = connectors.collect { it.port }
    portsInfo = (portsInfo.size() == 1 ? 'port ' : 'ports ') + portsInfo.join(', ')
    log.warn '{} started and listening on {}', params.servletContainerDescription, portsInfo

    def httpConn = configurer.findHttpConnector(server)
    def httpsConn = configurer.findHttpsConnector(server)

    for(def context in server.handler.handlers) {
      log.warn '{} runs at:', (context.displayName - '/')
      if(httpConn)
        log.warn '  http://{}:{}{}', httpConn.host, httpConn.port, context.contextPath
      if(httpsConn)
        log.warn '  https://{}:{}{}', httpsConn.host, httpsConn.port, context.contextPath
    }
  }

  @Override
  void stopServer() {
    if(server != null) {
      server.stop()
      server = null
      log.warn '{} stopped.', params.servletContainerDescription
      log = null
    }
  }
}

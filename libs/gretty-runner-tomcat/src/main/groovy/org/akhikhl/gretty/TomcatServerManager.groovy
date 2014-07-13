/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.apache.catalina.Context
import org.apache.catalina.connector.Connector
import org.apache.catalina.core.StandardContext
import org.apache.catalina.startup.Tomcat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler

/**
 *
 * @author akhikhl
 */
class TomcatServerManager implements ServerManager {

  private TomcatConfigurer configurer
  protected Map params
	protected Tomcat tomcat
  protected Logger log

  TomcatServerManager(TomcatConfigurer configurer) {
    this.configurer = configurer
  }

  @Override
  void setParams(Map params) {
    this.params = params
  }

  @Override
  void startServer() {
    assert tomcat == null

    if(params.logging)
      LoggingUtils.configureLogging(params.logging)
    else if(params.logbackConfig)
      LoggingUtils.useConfig(params.logbackConfig)

    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install()

    log = LoggerFactory.getLogger(this.getClass())
    configurer.setLogger(log)

    tomcat = new TomcatServerConfigurer().createAndConfigureServer(configurer, params)
    tomcat.start()

    def connectors = tomcat.service.findConnectors()
    
    def portsInfo = connectors.findConnectors().collect { it.port }
    portsInfo = (portsInfo.size() == 1 ? 'port ' : 'ports ') + portsInfo.join(', ')
    log.warn '{} started and listening on {}', params.servletContainerDescription, portsInfo
    
    Connector httpConn = connectors.find { it.scheme == 'http' }
    Connector httpsConn = connectors.find { it.scheme == 'https' }
    
    for(Context context in tomcat.host.findChildren().findAll { it instanceof Context }) {
      log.warn '{} runs at:', (context.name - '/')
      if(httpConn)
        log.warn '  http://{}:{}{}', tomcat.hostname, httpConn.port, context.path
      if(httpsConn)
        log.warn '  https://{}:{}{}', tomcat.hostname, httpsConn.port, context.path
    }
  }

  @Override
  void stopServer() {
    if(tomcat != null) {
      tomcat.stop()
      tomcat.getServer().await()
      tomcat.destroy()
      tomcat = null
      log.warn '{} stopped.', params.servletContainerDescription
      log = null
    }
  }
}

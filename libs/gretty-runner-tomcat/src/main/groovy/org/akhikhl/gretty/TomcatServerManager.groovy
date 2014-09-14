/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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
    startServer(null)
  }

  @Override
  void startServer(ServerStartEvent startEvent) {
    assert tomcat == null

    if(params.logging)
      LoggingUtils.configureLogging(params.logging)
    else if(params.logbackConfig)
      LoggingUtils.useConfig(params.logbackConfig)

    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install()

    log = LoggerFactory.getLogger(this.getClass())
    configurer.setLogger(log)

    tomcat = new TomcatServerConfigurer(configurer, params).createAndConfigureServer()
    tomcat.start()

    if(startEvent)
      startEvent.onServerStart(new TomcatServerStartInfo().getInfo(tomcat, null, params))
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

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

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
	protected Tomcat server
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
    assert server == null

    if(params.logging)
      LoggingUtils.configureLogging(params.logging)
    else if(params.logbackConfig)
      LoggingUtils.useConfig(params.logbackConfig)

    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install()

    log = LoggerFactory.getLogger(this.getClass())
    configurer.setLogger(log)

    server = new TomcatServerConfigurer().createAndConfigureServer(configurer, params)
    server.start()
  }

  @Override
  void stopServer() {
    if(server != null) {
      server.stop()
      server.getServer().await()
      server.destroy()
      server = null
      log = null
    }
  }
}

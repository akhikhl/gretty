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

/**
 *
 * @author akhikhl
 */
class TomcatServerManager implements ServerManager {

  private static final Logger log = LoggerFactory.getLogger(TomcatServerManager)

  private TomcatConfigurer configurer
  protected Map params
	protected Tomcat tomcat

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

    log.debug '{} starting.', params.servletContainerDescription

    tomcat = new TomcatServerConfigurer(configurer, params).createAndConfigureServer()
    tomcat.start()

    if(startEvent)
      startEvent.onServerStart(new TomcatServerStartInfo().getInfo(tomcat, null, params))

    log.debug '{} started.', params.servletContainerDescription
  }

  @Override
  void stopServer() {
    if(tomcat != null) {
      log.debug '{} stopping.', params.servletContainerDescription
      tomcat.stop()
      tomcat.getServer().await()
      tomcat.destroy()
      tomcat = null
      log.debug '{} stopped.', params.servletContainerDescription
    }
  }
}

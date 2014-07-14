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
  void startServer(ServerStartEvent startEvent) {
    assert server == null

    if(params.logging)
      LoggingUtils.configureLogging(params.logging)
    else if(params.logbackConfig)
      LoggingUtils.useConfig(params.logbackConfig)

    log = LoggerFactory.getLogger(this.getClass())
    configurer.setLogger(log)

    server = new JettyServerConfigurer().createAndConfigureServer(configurer, params)
    server.start()
    
    startEvent.onServerStart(new JettyServerStartInfo().getInfo(server, configurer, params))
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

/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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
    startServer(null)
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

    server = new JettyServerConfigurer(configurer, params).createAndConfigureServer()
    server.start()

    if(startEvent)
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

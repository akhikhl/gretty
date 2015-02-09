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

  private static final Logger log = LoggerFactory.getLogger(JettyServerManager)

  private JettyConfigurer configurer
  protected Map params
	protected server

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

    log.debug '{} starting.', params.servletContainerDescription

    server = new JettyServerConfigurer(configurer, params).createAndConfigureServer()
    server.start()

    if(startEvent)
      startEvent.onServerStart(new JettyServerStartInfo().getInfo(server, configurer, params))

    log.debug '{} started.', params.servletContainerDescription
  }

  @Override
  void stopServer() {
    if(server != null) {
      log.debug '{} stopping.', params.servletContainerDescription
      server.stop()
      server = null
      log.debug '{} stopped.', params.servletContainerDescription
    }
  }
}

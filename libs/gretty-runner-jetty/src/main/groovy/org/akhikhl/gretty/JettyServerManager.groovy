/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
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
  void startServer(ServerStartEvent startEvent) {
    assert server == null

    log.debug '{} starting.', params.servletContainerDescription

    server = new JettyServerConfigurer(configurer, params).createAndConfigureServer()

    boolean result = false
    try {
      server.start()
      result = true
    } catch(Throwable x) {
      log.error 'Error starting server', x
      if(startEvent) {
        Map startInfo = new JettyServerStartInfo().getInfo(server, configurer, params)
        startInfo.status = 'error starting server'
        startInfo.error = true
        startInfo.errorMessage = x.getMessage() ?: x.getClass().getName()
        StringWriter sw = new StringWriter()
        x.printStackTrace(new PrintWriter(sw))
        startInfo.stackTrace = sw.toString()
        startEvent.onServerStart(startInfo)
      } else
        throw x
    }

    if(result) {
      if(startEvent) {
        Map startInfo = new JettyServerStartInfo().getInfo(server, configurer, params)
        startEvent.onServerStart(startInfo)
      }
      log.debug '{} started.', params.servletContainerDescription
    }
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

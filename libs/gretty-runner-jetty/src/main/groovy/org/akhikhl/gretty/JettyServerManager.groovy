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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
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

    server = createServerConfigurer().createAndConfigureServer()

    boolean result = false
    try {
      server.start()
      result = true
    } catch(Throwable x) {
      log.error 'Error starting server', x
      if(x.getClass().getName() == 'org.eclipse.jetty.util.MultiException') {
        for(Throwable xx in x.getThrowables())
          log.error 'Error', xx
      }
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

  private JettyServerConfigurer createServerConfigurer() {
    new JettyServerConfigurer(configurer, params)
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

  @Override
  void redeploy(List<String> contextPaths) {
    log.debug('redeploying {}.', contextPaths.join(' '))
    def handlers = configurer.getHandlersByContextPaths(server, contextPaths)
    handlers.each {
      log.error("removing handlers: ${it}")
      configurer.removeHandlerFromServer(server, it)
    }
    //
    def contexts = contextPaths.collect { contextPath ->
      params.webApps.find { it.contextPath == contextPath }
    }.collect {
      def context = createServerConfigurer().createContext(it, new File(params.baseDir), server)
      configurer.addHandlerToServer(server, context)
      context
    }
    contexts.each { it.start() }
  }
}

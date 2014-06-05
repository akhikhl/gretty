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
    
    server = configurer.createServer()
    configurer.applyJettyXml(server, params.jettyXml)
    configurer.configureConnectors(server, params)

    List handlers = []

    for(def webapp in params.webApps) {
      def context = configurer.createWebAppContext(webapp.webappClassPath)
      configurer.addConfigurationClasses(context, webapp.webappClassPath)
      configurer.applyJettyEnvXml(context, webapp.jettyEnvXml)
      configurer.configureRealm(context, webapp.realm, webapp.realmConfigFile)

      context.setContextPath(webapp.contextPath)

      webapp.initParams?.each { key, value ->
        context.setInitParameter(key, value)
      }

      if(webapp.resourceBase != null) {
        if(webapp.inplace)
          context.setResourceBase(webapp.resourceBase)
        else
          context.setWar(webapp.resourceBase)
      }

      handlers.add(context)
    }

    configurer.setHandlersToServer(server, handlers)

    server.start()
  }

  @Override
  void stopServer() {
    if(server != null) {
      server.stop()
      server = null
      log = null
    }
  }
}

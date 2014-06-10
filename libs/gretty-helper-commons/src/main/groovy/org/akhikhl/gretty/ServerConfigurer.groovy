/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
class ServerConfigurer {

  static createAndConfigureServer(JettyConfigurer configurer, Map params, Closure configureContext = null) {

    def server = configurer.createServer()
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

      if(configureContext)
        configureContext(webapp, context)

      handlers.add(context)
    }

    configurer.setHandlersToServer(server, handlers)

    return server
  }
}


/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.apache.commons.io.FilenameUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class JettyServerConfigurer {

  protected final Logger log

  JettyServerConfigurer() {
    log = LoggerFactory.getLogger(this.getClass())
  }

  def createAndConfigureServer(JettyConfigurer configurer, Map params, Closure configureContext = null) {

    def server = configurer.createServer()
    
    File baseDir = new File(params.baseDir)
    new File(baseDir, 'webapps').mkdirs()
    
    configurer.applyJettyXml(server, params.serverConfigFile)
    configurer.configureConnectors(server, params)

    List handlers = []

    for(def webapp in params.webApps) {
      def context = configurer.createWebAppContext(webapp.webappClassPath)
      context.setDisplayName(webapp.contextPath)
      context.setContextPath(webapp.contextPath)
      
      if(!params.supressSetConfigurations)
        configurer.setConfigurationsToWebAppContext(context, configurer.getConfigurations(webapp.webappClassPath))
      configurer.applyJettyEnvXml(context, webapp.jettyEnvXml)
      
      log.warn 'jetty context temp directory: {}', new File(baseDir, 'webapps' + context.getContextPath())
      //context.setTempDirectory(new File(baseDir, 'webapps' + context.getContextPath()))
      //context.setPersistTempDirectory(true)

      webapp.initParams?.each { key, value ->
        context.setInitParameter(key, value)
      }

      if(new File(webapp.resourceBase).isDirectory())
        context.setResourceBase(webapp.resourceBase)
      else
        context.setWar(webapp.resourceBase)

      configurer.configureSecurity(context, params, webapp)

      configurer.configureSessionManager(server, context, params, webapp)

      if(configureContext)
        configureContext(webapp, context)

      handlers.add(context)
    }

    configurer.setHandlersToServer(server, handlers)

    return server
  }
}

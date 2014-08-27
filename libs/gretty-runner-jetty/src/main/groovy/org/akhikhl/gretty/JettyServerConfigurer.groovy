/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.jar.JarEntry
import java.util.jar.JarFile
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
      context.displayName = webapp.contextPath
      context.contextPath = webapp.contextPath

      if(!params.supressSetConfigurations)
        configurer.setConfigurationsToWebAppContext(context, configurer.getConfigurations(webapp.webappClassPath))

      File tempDir = new File(baseDir, 'webapps-exploded' + context.contextPath)
      tempDir.mkdirs()
      log.debug 'jetty context temp directory: {}', tempDir
      context.setTempDirectory(tempDir)
      if(context.respondsTo('setPersistTempDirectory')) // not supported on older jetty versions
        context.setPersistTempDirectory(true)

      webapp.initParams?.each { key, value ->
        context.setInitParameter(key, value)
      }

      if(new File(webapp.resourceBase).isDirectory()) {
        if(webapp.extraResourceBases)
          context.setBaseResource(configurer.createResourceCollection([ webapp.resourceBase ] + webapp.extraResourceBases))
        else
          context.setResourceBase(webapp.resourceBase)
      }
      else
        context.setWar(webapp.resourceBase)

      URL contextConfigFile = getContextConfigFile(params.servletContainerId, context.baseResource)
      if(!contextConfigFile && webapp.contextConfigFile)
        contextConfigFile = new File(webapp.contextConfigFile).toURI().toURL()
      configurer.applyContextConfigFile(context, contextConfigFile)

      String realm = webapp.realm ?: params.realm
      URL realmConfigFile = getRealmFile(params.servletContainerId, context.baseResource)
      if(!realmConfigFile) {
        if(webapp.realmConfigFile)
          realmConfigFile = new File(webapp.realmConfigFile).toURI().toURL()
        else if(params.realmConfigFile)
          realmConfigFile = new File(params.realmConfigFile).toURI().toURL()
      }
      if(realm && realmConfigFile) {
        if(context.securityHandler.loginService == null) {
          log.info 'Configuring {} with realm \'{}\', {}', context.contextPath, realm, realmConfigFile
          configurer.configureSecurity(context, realm, realmConfigFile.toString(), params.singleSignOn ?: false)
        } else
          log.warn 'loginService is already configured, ignoring realm \'{}\', {}', realm, realmConfigFile
      }

      configurer.configureSessionManager(server, context, params, webapp)

      if(configureContext)
        configureContext(webapp, context)

      handlers.add(context)
    }

    configurer.setHandlersToServer(server, handlers)

    return server
  }

  URL getContextConfigFile(String servletContainer, baseResource) {
    for(def possibleFileName in [ servletContainer + '-env.xml', 'jetty-env.xml' ]) {
      URL url = resolveContextFile(baseResource, 'META-INF/' + possibleFileName)
      if(url)
        return url
    }
    null
  }

  URL getRealmFile(String servletContainer, baseResource) {
    for(def possibleFileName in [ servletContainer + '-realm.properties', 'jetty-realm.properties' ]) {
      URL url = resolveContextFile(baseResource, 'META-INF/' + possibleFileName)
      if(url)
        return url
    }
    null
  }

  URL resolveContextFile(baseResource, String path) {
    baseResource.findResource(path)?.url
  }
}

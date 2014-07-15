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

      if(new File(webapp.resourceBase).isDirectory())
        context.setResourceBase(webapp.resourceBase)
      else
        context.setWar(webapp.resourceBase)

      configurer.configureSecurity(context, params, webapp)

      configurer.configureSessionManager(server, context, params, webapp)

      URL contextConfigFile = getContextConfigFile(params.servletContainer.id, webapp.resourceBase)
      if(!contextConfigFile && webapp.contextConfigFile)
        contextConfigFile = new File(webapp.contextConfigFile).toURI().toURL()
      configurer.applyContextConfigFile(context, contextConfigFile)

      if(configureContext)
        configureContext(webapp, context)

      handlers.add(context)
    }

    configurer.setHandlersToServer(server, handlers)

    return server
  }

  URL getContextConfigFile(String servletContainer, String resourceBase) {
    def possibleSubDirs = [ 'META-INF', 'WEB-INF' ]
    def possibleFileNames = [ servletContainer + '-env.xml', 'jetty-env.xml' ]
    for(def possibleSubDir in possibleSubDirs)
      for(def possibleFileName in possibleFileNames) {
        URL url = resolveContextFile(resourceBase, possibleSubDir + '/' + possibleFileName)
        if(url)
          return url
      }
    null
  }

  URL resolveContextFile(String resourceBase, String file) {
    File docBase = new File(resourceBase)
    if (docBase.isDirectory()) {
      File configFile = new File(docBase, file)
      if(configFile.exists())
        return configFile.toURI().toURL()
    } else {
      JarFile jar = new JarFile(docBase)
      try {
        JarEntry entry = jar.getJarEntry(file)
        if (entry != null)
          return new URL('jar:' + docBase.toURI().toString() + '!/' + file)
      } finally {
        jar.close()
      }
    }
    null
  }
}

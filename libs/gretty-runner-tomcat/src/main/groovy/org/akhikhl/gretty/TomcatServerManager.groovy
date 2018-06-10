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
import org.apache.catalina.startup.Tomcat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class TomcatServerManager implements ServerManager {

  private static final Logger log = LoggerFactory.getLogger(TomcatServerManager)

  private TomcatConfigurer configurer
  protected Map params
	protected Tomcat tomcat

  TomcatServerManager(TomcatConfigurer configurer) {
    this.configurer = configurer
  }

  private TomcatServerConfigurer createServerConfigurer() {
    return new TomcatServerConfigurer(configurer, params);
  }

  @Override
  void setParams(Map params) {
    this.params = params
  }

  @Override
  void startServer(ServerStartEvent startEvent) {
    assert tomcat == null

    log.debug '{} starting.', params.servletContainerDescription

    tomcat = createServerConfigurer().createAndConfigureServer()

    boolean result = false
    try {
      tomcat.start()
      result = true
    } catch(Throwable x) {
      log.error 'Error starting server', x
      if(startEvent) {
        Map startInfo = new TomcatServerStartInfo().getInfo(tomcat, null, params)
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
      if (startEvent) {
        Map startInfo = new TomcatServerStartInfo().getInfo(tomcat, null, params)
        startEvent.onServerStart(startInfo)
      }
      log.debug '{} started.', params.servletContainerDescription
    }
  }

  @Override
  void stopServer() {
    if(tomcat != null) {
      log.debug '{} stopping.', params.servletContainerDescription
      tomcat.stop()
      tomcat.getServer().await()
      tomcat.destroy()
      tomcat = null
      log.debug '{} stopped.', params.servletContainerDescription
    }
  }

  @Override
  void redeploy(List<String> webapps) {
    if(tomcat != null) {
      log.debug 'redeploying {}.', webapps.join(", ")
      def containers = webapps.collect { TomcatServerConfigurer.getEffectiveContextPath(it) }.collect { tomcat.host.findChild(it) }
      //
      containers.each { tomcat.host.removeChild(it) }
      webapps.collect { contextPath -> params.webApps.find { it.contextPath == contextPath}}.each {
        def context = createServerConfigurer().createContext(it, tomcat)
        tomcat.host.addChild(context)
      }
    }
  }
}

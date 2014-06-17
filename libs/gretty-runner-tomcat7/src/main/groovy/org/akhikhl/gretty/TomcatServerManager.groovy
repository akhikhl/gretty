/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.apache.catalina.core.StandardContext
import org.apache.catalina.loader.WebappLoader
import org.apache.catalina.startup.ContextConfig
import org.apache.catalina.startup.Tomcat
import org.apache.catalina.startup.Tomcat.DefaultWebXmlListener
import org.apache.catalina.startup.Tomcat.FixContextListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class TomcatServerManager implements ServerManager {

  protected Map params
	protected server
  protected Logger log

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

    org.slf4j.bridge.SLF4JBridgeHandler.install()

    log = LoggerFactory.getLogger(this.getClass())

    Tomcat tomcat = new Tomcat()
    File tempDir = new File(System.getProperty('java.io.tmpdir'), 'tomcat-' + UUID.randomUUID().toString())
    tempDir.mkdirs()
    tempDir.deleteOnExit()
    tomcat.setBaseDir(tempDir.absolutePath)

		tomcat.getHost().setAutoDeploy(true)
		tomcat.getEngine().setBackgroundProcessorDelay(-1)

    if(params.host)
      tomcat.setHostname(params.host)

    if(params.httpPort)
      tomcat.setPort(params.httpPort)

    for(def webapp in params.webApps) {
      StandardContext context = new StandardContext()
      context.setName(webapp.contextPath)
      context.setPath(webapp.contextPath)
      context.setDocBase(webapp.resourceBase)
      context.addLifecycleListener(new FixContextListener())
      URL[] classpathUrls = (webapp.webappClassPath.collect { new URL(it) }) as URL[]
      ClassLoader classLoader = new URLClassLoader(classpathUrls, this.getClass().getClassLoader())
      context.setParentClassLoader(classLoader)
      WebappLoader loader = new WebappLoader(classLoader)
      loader.setLoaderClass(TomcatEmbeddedWebappClassLoader.class.getName())
      loader.setDelegate(true)
      context.setLoader(loader)
      context.addLifecycleListener(new DefaultWebXmlListener())
      context.addLifecycleListener(new ContextConfig())
      tomcat.getHost().addChild(context)
    }
    tomcat.start()
    server = tomcat
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

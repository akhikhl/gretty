/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import javax.naming.directory.DirContext
import javax.servlet.ServletContext
import org.apache.catalina.Container
import org.apache.catalina.Context
import org.apache.catalina.Host
import org.apache.catalina.Lifecycle
import org.apache.catalina.LifecycleEvent
import org.apache.catalina.LifecycleListener
import org.apache.catalina.Server
import org.apache.catalina.connector.Connector
import org.apache.catalina.core.StandardContext
import org.apache.catalina.deploy.WebXml
import org.apache.catalina.loader.WebappLoader
import org.apache.catalina.startup.ContextConfig
import org.apache.catalina.startup.Tomcat
import org.apache.catalina.startup.Tomcat.DefaultWebXmlListener
import org.apache.catalina.startup.Tomcat.FixContextListener
import org.apache.naming.resources.BaseDirContext
import org.apache.naming.resources.FileDirContext
import org.apache.tomcat.JarScanner
import org.apache.tomcat.JarScannerCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.InputSource

import org.apache.tomcat.util.scan.JarFactory

/**
 *
 * @author ahi
 */
class TomcatServerConfigurer {

  protected final Logger log

  TomcatServerConfigurer() {
    log = LoggerFactory.getLogger(this.getClass())
  }

  Tomcat createAndConfigureServer(Map params, Closure configureContext = null) {

    Tomcat tomcat = new Tomcat()
    File tempDir = new File(System.getProperty('java.io.tmpdir'), 'tomcat-' + UUID.randomUUID().toString())
    new File(tempDir, 'webapps').mkdirs()
    tempDir.deleteOnExit()
    tomcat.setBaseDir(tempDir.absolutePath)

		tomcat.getHost().setAutoDeploy(true)
		tomcat.getEngine().setBackgroundProcessorDelay(-1)

    if(params.host)
      tomcat.setHostname(params.host)

    if(params.httpPort) {
      final Connector httpConn = new Connector('HTTP/1.1')
      httpConn.setScheme('http')
      httpConn.setPort(params.httpPort)
      httpConn.setProperty('maxPostSize', '0')  // unlimited
      if(params.httpIdleTimeout)
        httpConn.setProperty('keepAliveTimeout', params.httpIdleTimeout)
      if(params.httpsPort)
        httpConn.setRedirectPort(params.httpsPort)
      tomcat.getService().addConnector(httpConn)
      tomcat.setConnector(httpConn)
    }

    if(params.httpsPort) {
      final Connector httpsConn = new Connector('HTTP/1.1')
      httpsConn.setScheme('https')
      httpsConn.setPort(params.httpsPort)
      httpsConn.setProperty('maxPostSize', '0')  // unlimited
      httpsConn.setSecure(true)
      httpsConn.setProperty('SSLEnabled', 'true')
      if(params.sslKeyManagerPassword)
        httpsConn.setProperty('keyPass', params.sslKeyManagerPassword)
      if(params.sslKeyStorePath)
        httpsConn.setProperty('keystoreFile', params.sslKeyStorePath)
      if(params.sslKeyStorePassword)
        httpsConn.setProperty('keystorePass', params.sslKeyStorePassword)
      if(params.sslTrustStorePath)
        httpsConn.setProperty('truststoreFile', params.sslTrustStorePath)
      if(params.sslTrustStorePassword)
        httpsConn.setProperty('truststorePass', params.sslTrustStorePassword)
      if(params.httpsIdleTimeout)
        httpsConn.setProperty('keepAliveTimeout', params.httpsIdleTimeout)
      tomcat.getService().addConnector(httpsConn)
      if(!params.httpPort)
        tomcat.setConnector(httpsConn)
    }

    for(def webapp in params.webApps) {
      StandardContext context = (params.contextClass ? params.contextClass.newInstance() : new StandardContext() {
        def log = LoggerFactory.getLogger(this.getClass())
        DirContext webappResources
        public void addResourceJarUrl(URL url) {
          super.addResourceJarUrl(url)
          log.warn 'addResourceJarUrl {}', url
          def p = webappResources.getRealPath('page1.htm')
          log.warn 'webappResources.getRealPath("page1.htm") -> {}', p
        }
        public String getRealPath(String path) {
          def result = super.getRealPath(path)
          log.warn '*** getRealPath {} -> {}', path, result
          result
        }
        public boolean resourcesStart() {
          def result = super.resourcesStart()
          log.warn 'resourcesStart -> {}', result
          result
        }
        public synchronized void setResources(DirContext resources) {
          webappResources = resources
          super.setResources(webappResources)
        }
      })
      context.setName(webapp.contextPath)
      context.setPath(webapp.contextPath)
      context.setDocBase(webapp.resourceBase)
      context.addLifecycleListener(new FixContextListener())
      // context.setLogEffectiveWebXml(true) // enable for debugging webxml merge
      ClassLoader parentClassLoader = params.parentClassLoader ?: this.getClass().getClassLoader()
      URL[] classpathUrls = (webapp.webappClassPath ?: []).collect { new URL(it) } as URL[]
      ClassLoader classLoader = new URLClassLoader(classpathUrls, parentClassLoader)
      context.addLifecycleListener(new SpringloadedCleanup())
      context.setParentClassLoader(classLoader)
      SkipPatternJarScanner.apply(context, '')
      WebappLoader loader = new WebappLoader(classLoader)
      loader.setLoaderClass(TomcatEmbeddedWebappClassLoader.class.getName())
      loader.setDelegate(true)
      context.setLoader(loader)
      context.addLifecycleListener(new ContextConfig() {
        def log = LoggerFactory.getLogger(this.getClass())
        protected synchronized void configureStart() {
          log.warn 'ContextConfig.configureStart BEFORE'
          super.configureStart()
          log.warn 'ContextConfig.configureStart AFTER'
        }
      })
      //fixAnnotationResources(classpathUrls, context)

      if(configureContext)
        configureContext(webapp, context)

      tomcat.getHost().addChild(context)
    }

    tomcat
  }

  private void fixAnnotationResources(URL[] classpathUrls, StandardContext context) {
    // need to fix resource paths for tomcat7 in order to make annotations work
    Class VirtualDirContext
    try {
      VirtualDirContext = Class.forName('org.apache.naming.resources.VirtualDirContext', true, this.class.getClassLoader())
    } catch(ClassNotFoundException e) {
      // we are on newer version of tomcat, so annotations will work and there's nothing to fix.
      return
    }
    String extraResourcePaths = classpathUrls.collect { it.path }.findAll { !it.endsWith('.jar') }.collect { '/WEB-INF/classes=' + it }.join(',')
    def resources = VirtualDirContext.newInstance()
    resources.setExtraResourcePaths(extraResourcePaths)
    log.warn 'context.setResources {}', resources
    context.setResources(resources)
  }

  private class SpringloadedCleanup implements LifecycleListener {

		@Override
		public void lifecycleEvent(LifecycleEvent event) {
      if(event.getType() == Lifecycle.BEFORE_STOP_EVENT)
        cleanup(event.getLifecycle())
    }

    protected void cleanup(StandardContext context) {
      def TypeRegistry
      try {
        TypeRegistry = Class.forName('org.springsource.loaded.TypeRegistry', true, this.class.getClassLoader())
      } catch(ClassNotFoundException e) {
        // springloaded not present, just ignore
        return
      }
      ClassLoader classLoader = context.getLoader().getClassLoader()
      while(classLoader != null) {
        def typeRegistry = TypeRegistry.getTypeRegistryFor(classLoader)
        if(typeRegistry != null && typeRegistry.@fsWatcher != null) {
          log.info 'springloaded shutdown: {}', typeRegistry.@fsWatcher.@thread
          typeRegistry.@fsWatcher.shutdown()
          typeRegistry.@fsWatcher.@thread.join()
        }
        classLoader = classLoader.getParent()
      }
    }
  }
}

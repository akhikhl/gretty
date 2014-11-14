/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import org.apache.catalina.Host
import org.apache.catalina.Lifecycle
import org.apache.catalina.LifecycleEvent
import org.apache.catalina.LifecycleListener
import org.apache.catalina.authenticator.SingleSignOn
import org.apache.catalina.connector.Connector
import org.apache.catalina.core.StandardContext
import org.apache.catalina.core.StandardService
import org.apache.catalina.loader.WebappLoader
import org.apache.catalina.realm.MemoryRealm
import org.apache.catalina.startup.Catalina
import org.apache.catalina.startup.ContextConfig
import org.apache.catalina.startup.Tomcat
import org.apache.catalina.startup.Tomcat.DefaultWebXmlListener
import org.apache.catalina.startup.Tomcat.FixContextListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.InputSource

/**
 *
 * @author akhikhl
 */
class TomcatServerConfigurer {

  protected final Logger log
  protected final TomcatConfigurer configurer
  protected final Map params

  TomcatServerConfigurer(TomcatConfigurer configurer, Map params) {
    log = LoggerFactory.getLogger(this.getClass())
    this.configurer = configurer
    this.params = params
  }

  Tomcat createAndConfigureServer(Closure configureContext = null) {

    Tomcat tomcat = new Tomcat()

    if(params.enableNaming)
      tomcat.enableNaming()

    File baseDir = new File(params.baseDir)
    new File(baseDir, 'webapps').mkdirs()

    File tempDir = new File(baseDir, 'temp')

    def service
    def connectors

    if(params.serverConfigFile) {
      def catalina = new Catalina()
      def digester = catalina.createStartDigester()
      new File(params.serverConfigFile).withInputStream {
        def inputSource = new InputSource(params.serverConfigFile)
        inputSource.setByteStream(it)
        digester.push(catalina)
        digester.parse(inputSource)
      }
      def server = tomcat.server = catalina.getServer()
      def services = server.findServices()
      assert services.length == 1
      service = services[0]
      tomcat.service = service
      tomcat.engine = service.getContainer()
      connectors = service.findConnectors()
      tomcat.host = service.getContainer().findChildren().find { it instanceof Host }
      tomcat.port = connectors[0].port
      tomcat.hostname = tomcat.host.name
      server.setCatalina(catalina)
      configurer.setBaseDir(tomcat, baseDir)
    } else {
      configurer.setBaseDir(tomcat, baseDir)
      tomcat.engine.backgroundProcessorDelay = -1
      tomcat.host.autoDeploy = true
      service = tomcat.service
      connectors = service.findConnectors()
    }

    if(!tomcat.hostname)
      tomcat.hostname = params.host ?: ServerDefaults.defaultHost

    Connector httpConn = connectors.find { it.scheme == 'http' }

    boolean newHttpConnector = false
    if(params.httpEnabled && !httpConn) {
      newHttpConnector = true
      httpConn = new Connector('HTTP/1.1')
      httpConn.scheme = 'http'
    }

    if(httpConn) {
      if(!httpConn.port || httpConn.port < 0)
        httpConn.port = params.httpPort ?: ServerDefaults.defaultHttpPort

      if(params.httpIdleTimeout)
        httpConn.setProperty('keepAliveTimeout', params.httpIdleTimeout)

      httpConn.setProperty('maxPostSize', '0') // unlimited post size

      if(newHttpConnector) {
        service.addConnector(httpConn)
        connectors = service.findConnectors()
      }
    }

    Connector httpsConn = connectors.find { it.scheme == 'https' }

    boolean newHttpsConnector = false
    if(params.httpsEnabled && !httpsConn) {
        newHttpsConnector = true
        httpsConn = new Connector('HTTP/1.1')
        httpsConn.scheme = 'https'
        httpsConn.secure = true
        httpsConn.setProperty('SSLEnabled', 'true')
    }

    if(httpsConn) {
      if(!httpsConn.port || httpsConn.port < 0)
        httpsConn.port = params.httpsPort ?: ServerDefaults.defaultHttpsPort

      if(params.sslKeyManagerPassword)
        httpsConn.setProperty('keyPass', params.sslKeyManagerPassword)
      if(params.sslKeyStorePath) {
        if(params.sslKeyStorePath.startsWith('classpath:')) {
          String resString = params.sslKeyStorePath - 'classpath:'
          File keystoreFile = new File(tempDir, 'keystore')
          keystoreFile.parentFile.mkdirs()
          if(keystoreFile.exists())
            keystoreFile.delete()
          def stm = getClass().getResourceAsStream(resString)
          if(stm == null)
            throw new Exception("Could not resource referenced in sslKeyStorePath: '${resString}'")
          stm.withStream {
            keystoreFile.withOutputStream { outs ->
              outs << stm
            }
          }
          httpsConn.setProperty('keystoreFile', keystoreFile.absolutePath)
        }
        else
          httpsConn.setProperty('keystoreFile', params.sslKeyStorePath)
      }
      if(params.sslKeyStorePassword)
        httpsConn.setProperty('keystorePass', params.sslKeyStorePassword)
      if(params.sslTrustStorePath) {
        if(params.sslTrustStorePath.startsWith('classpath:')) {
          String resString = params.sslTrustStorePath - 'classpath:'
          File truststoreFile = new File(tempDir, 'truststore')
          truststoreFile.parentFile.mkdirs()
          if(truststoreFile.exists())
            truststoreFile.delete()
          def stm = getClass().getResourceAsStream(resString)
          if(stm == null)
            throw new Exception("Could not resource referenced in sslTrustStorePath: '${resString}'")
          stm.withStream {
            truststoreFile.withOutputStream { outs ->
              outs << stm
            }
          }
          httpsConn.setProperty('truststoreFile', truststoreFile.absolutePath)
        }
        else
          httpsConn.setProperty('truststoreFile', params.sslTrustStorePath)
      }
      if(params.sslTrustStorePassword)
        httpsConn.setProperty('truststorePass', params.sslTrustStorePassword)

      if(params.httpsIdleTimeout)
        httpsConn.setProperty('keepAliveTimeout', params.httpsIdleTimeout)

      httpsConn.setProperty('maxPostSize', '0')  // unlimited

      if(newHttpsConnector) {
        service.addConnector(httpsConn)
        connectors = service.findConnectors()
      }
    }

    if(httpConn && httpsConn)
      httpConn.redirectPort = httpsConn.port

    if(httpConn)
      tomcat.setConnector(httpConn)
    else if(httpsConn)
      tomcat.setConnector(httpsConn)
    else if(connectors.length != 0)
      tomcat.setConnector(connectors[0])

    if(params.singleSignOn && !tomcat.host.pipeline.valves.find { it instanceof SingleSignOn })
      tomcat.host.addValve(new SingleSignOn())

    for(Map webapp in params.webApps) {
      StandardContext context = params.contextClass ? params.contextClass.newInstance() : new StandardContext()
      context.setName(webapp.contextPath)
      context.setPath(webapp.contextPath)
      configurer.setResourceBase(context, webapp)
      // context.setLogEffectiveWebXml(true) // enable for debugging webxml merge
      ClassLoader parentClassLoader = params.parentClassLoader ?: this.getClass().getClassLoader()
      URL[] classpathUrls = (webapp.webappClassPath ?: []).collect { new URL(it) } as URL[]
      ClassLoader classLoader = new URLClassLoader(classpathUrls, parentClassLoader)
      classLoader.metaClass.webappClassLoader = true
      context.addLifecycleListener(new SpringloadedCleanup())
      context.setParentClassLoader(classLoader)
      context.setJarScanner(configurer.createJarScanner(context.getJarScanner(), new JarSkipPatterns()))
      WebappLoader loader = new WebappLoader(classLoader)
      loader.setLoaderClass(TomcatEmbeddedWebappClassLoader.class.getName())
      loader.setDelegate(true)
      context.setLoader(loader)

      def realmConfigFile = webapp.realmConfigFile ?: params.realmConfigFile
      if(realmConfigFile && new File(realmConfigFile).exists()) {
        log.info '{} -> realm config {}', webapp.contextPath, realmConfigFile
        def realm = new MemoryRealm()
        realm.setPathname(realmConfigFile)
        context.setRealm(realm)
      } else
        context.addLifecycleListener(new FixContextListener())

      context.configFile = tomcat.getWebappConfigFile(webapp.resourceBase, webapp.contextPath)
      if(!context.configFile && webapp.contextConfigFile)
        context.configFile = new File(webapp.contextConfigFile).toURI().toURL()
      if(context.configFile)
        log.info 'Configuring {} with {}', webapp.contextPath, context.configFile

      context.addLifecycleListener(configurer.createContextConfig(classpathUrls))

      if(configureContext) {
        configureContext.delegate = this
        configureContext(webapp, context)
      }

      if(!context.findChild('default'))
        context.addLifecycleListener(new DefaultWebXmlListener())

      if(log.isDebugEnabled())
        context.addLifecycleListener(new LifecycleListener() {
          @Override
          public void lifecycleEvent(LifecycleEvent event) {
            if (event.type == Lifecycle.CONFIGURE_START_EVENT) {
              def pipeline = context.getPipeline()
              log.debug 'START: context={}, pipeline: {} #{}', context.path, pipeline, System.identityHashCode(pipeline)
              log.debug '  valves:'
              for(def v in pipeline.getValves())
                log.debug '    {} #{}', v, System.identityHashCode(v)
            }
          }
        })

      tomcat.host.addChild(context)
    }

    tomcat
  }
}

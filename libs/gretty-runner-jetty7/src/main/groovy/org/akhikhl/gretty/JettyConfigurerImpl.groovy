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
import org.eclipse.jetty.plus.webapp.EnvConfiguration
import org.eclipse.jetty.plus.webapp.PlusConfiguration
import org.eclipse.jetty.security.HashLoginService
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.server.session.HashSessionManager
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.server.ssl.SslSocketConnector
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.util.resource.ResourceCollection
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.eclipse.jetty.webapp.*
import org.eclipse.jetty.xml.XmlConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class JettyConfigurerImpl implements JettyConfigurer {

  private static final Logger log = LoggerFactory.getLogger(JettyConfigurerImpl)

  private SSOAuthenticatorFactory ssoAuthenticatorFactory
  private HashSessionManager sharedSessionManager

  @Override
  def addLifeCycleListener(lifecycle, listener) {
    def lifeCycleListener = listener as LifeCycleListenerAdapter
    lifecycle.addLifeCycleListener(lifeCycleListener)
    lifeCycleListener
  }

  @Override
  void applyContextConfigFile(webAppContext, URL contextConfigFile) {
    if(contextConfigFile) {
      log.info 'Configuring {} with {}', webAppContext.contextPath, contextConfigFile
      XmlConfiguration xmlConfiguration = new XmlConfiguration(contextConfigFile)
      xmlConfiguration.configure(webAppContext)
    }
  }

  @Override
  void applyJettyXml(server, String jettyXml) {
    if(jettyXml != null) {
      log.info 'Configuring server with {}', jettyXml
      XmlConfiguration xmlConfiguration = new XmlConfiguration(new File(jettyXml).toURI().toURL())
      xmlConfiguration.configure(server)
    }
  }

  @Override
  void configureConnectors(server, Map params) {

    Connector httpConn = findHttpConnector(server)

    boolean newConnector = false
    if(params.httpEnabled && !httpConn) {
      newConnector = true
      httpConn = new SocketConnector()
      httpConn.soLingerTime = -1
    }

    if(httpConn) {
      if(!httpConn.host)
        httpConn.host = params.host ?: ServerDefaults.defaultHost

      if(!httpConn.port)
        httpConn.port = params.httpPort ?: ServerDefaults.defaultHttpPort

      if(httpConn.port == PortUtils.RANDOM_FREE_PORT)
        httpConn.port = PortUtils.findFreePort()

      if(params.httpIdleTimeout)
        httpConn.maxIdleTime = params.httpIdleTimeout

      if(newConnector)
        server.addConnector(httpConn)
    }

    Connector httpsConn = findHttpsConnector(server)

    boolean newHttpsConnector = false
    if(params.httpsEnabled && !httpsConn) {
      newHttpsConnector = true
      httpsConn = new SslSocketConnector(new SslContextFactory())
      httpsConn.soLingerTime = -1
    }

    if(httpsConn) {
      if(!httpsConn.host)
        httpsConn.host = params.host ?: ServerDefaults.defaultHost

      if(!httpsConn.port)
        httpsConn.port = params.httpsPort ?: ServerDefaults.defaultHttpsPort

      def sslContextFactory = httpsConn.getSslContextFactory()
      if(params.sslKeyStorePath) {
        if(params.sslKeyStorePath.startsWith('classpath:')) {
          String resString = params.sslKeyStorePath - 'classpath:'
          URL url = getClass().getResource(resString)
          if(url == null)
            throw new Exception("Could not resource referenced in sslKeyStorePath: '${resString}'")
            sslContextFactory.setKeyStoreResource(Resource.newResource(url))
        }
        else
          sslContextFactory.setKeyStorePath(params.sslKeyStorePath)
      }
      if(params.sslKeyStorePassword)
        sslContextFactory.setKeyStorePassword(params.sslKeyStorePassword)
      if(params.sslKeyManagerPassword)
        sslContextFactory.setKeyManagerPassword(params.sslKeyManagerPassword)
      if(params.sslTrustStorePath) {
        if(params.sslTrustStorePath.startsWith('classpath:')) {
          String resString = params.sslTrustStorePath - 'classpath:'
          URL url = getClass().getResource(resString)
          if(url == null)
            throw new Exception("Could not resource referenced in sslTrustStorePath: '${resString}'")
          sslContextFactory.setTrustStoreResource(Resource.newResource(url))
        }
        else
          sslContextFactory.setTrustStorePath(params.sslTrustStorePath)
      }
      if(params.sslTrustStorePassword)
        sslContextFactory.setTrustStorePassword(params.sslTrustStorePassword)
      if(params.sslNeedClientAuth)
        sslContextFactory.setNeedClientAuth(params.sslNeedClientAuth)

      if(params.httpsIdleTimeout)
        httpsConn.maxIdleTime = params.httpsIdleTimeout

      if(newHttpsConnector)
        server.addConnector(httpsConn)
    }
  }

  @Override
  void configureSecurity(context, String realm, String realmConfigFile, boolean singleSignOn) {
    context.securityHandler.loginService = new HashLoginService(realm, realmConfigFile)
    if(singleSignOn) {
      if(ssoAuthenticatorFactory == null)
        ssoAuthenticatorFactory = new SSOAuthenticatorFactory()
      context.securityHandler.authenticatorFactory = ssoAuthenticatorFactory
    }
  }

  @Override
  void configureSessionManager(server, context, Map serverParams, Map webappParams) {
    HashSessionManager sessionManager
    if(serverParams.singleSignOn) {
      sessionManager = sharedSessionManager
      if(sessionManager == null) {
        sessionManager = sharedSessionManager = new HashSessionManager() {

        }
        sessionManager.setMaxInactiveInterval(60 * 30) // 30 minutes
        sessionManager.setSessionPath('/')
      }
    } else {
      sessionManager = new HashSessionManager()
      sessionManager.setMaxInactiveInterval(60 * 30) // 30 minutes
    }
    def sessionHandler = new SessionHandler(sessionManager)
    // By setting server we fix bug with older jetty versions: sessionHandler produces NullPointerException, when session manager is reassigned.
    sessionHandler.setServer(server)
    context.setSessionHandler(sessionHandler)
  }

  @Override
  def createResourceCollection(List paths) {
    new ResourceCollection(paths as String[])
  }

  @Override
  def createServer() {
    // fix for issue https://github.com/akhikhl/gretty/issues/24
    org.eclipse.jetty.util.resource.Resource.defaultUseCaches = false
    return new Server()
  }

  @Override
  def createWebAppContext(Map serverParams, Map webappParams) {
    List<String> webappClassPath = webappParams.webappClassPath
    JettyWebAppContext context = new JettyWebAppContext()
    context.setWebInfLib(webappClassPath.findAll { it.endsWith('.jar') }.collect { new File(it) })
    context.setExtraClasspath(webappClassPath.collect { it.endsWith('.jar') ? it : (it.endsWith('/') ? it : it + '/') }.join(';'))
    if (webappParams.webXml != null) context.setDescriptor(webappParams.webXml);
    FilteringClassLoader classLoader = new FilteringClassLoader(context)
    classLoader.addServerClass('ch.qos.logback.')
    classLoader.addServerClass('org.slf4j.')
    classLoader.addServerClass('org.codehaus.groovy.')
    classLoader.addServerClass('groovy.')
    classLoader.addServerClass('groovyx.')
    classLoader.addServerClass('groovyjarjarantlr.')
    classLoader.addServerClass('groovyjarjarasm.')
    classLoader.addServerClass('groovyjarjarcommonscli.')
    context.classLoader = classLoader
    context.addLifeCycleListener(new LifeCycleListenerAdapter() {
      public void lifeCycleStopped(LifeCycle event) {
        context.classLoader = null
      }
    })
    context.setOverrideDescriptor('/org/akhikhl/gretty/override-web.xml')
    return context
  }

  @Override
  def findHttpConnector(server) {
    server.connectors.find { (it instanceof SocketConnector) && !(it instanceof SslSocketConnector) }
  }

  @Override
  def findHttpsConnector(server) {
    server.connectors.find { it instanceof SslSocketConnector }
  }

  @Override
  URL findResourceURL(baseResource, String path) {
    Resource res
    if(baseResource instanceof ResourceCollection)
      res = baseResource.findResource(path)
    else
      res = baseResource.addPath(path)
    if(res.exists())
      return res.getURL()
    null
  }

  @Override
  List getConfigurations(Map webappParams) {
    [
      new WebInfConfigurationEx(),
      new WebXmlConfiguration(),
      new MetaInfConfiguration(),
      new FragmentConfiguration(),
      new EnvConfiguration(),
      new PlusConfiguration(),
      new JettyWebXmlConfiguration()
    ]
  }

  @Override
  void removeLifeCycleListener(lifecycle, listener) {
    lifecycle.removeLifeCycleListener(listener)
  }

  @Override
  void setConfigurationsToWebAppContext(webAppContext, List configurations) {
    webAppContext.setConfigurations(configurations as Configuration[])
  }

  private ContextHandlerCollection findContextHandlerCollection(Handler handler) {
    if(handler instanceof ContextHandlerCollection)
      return handler
    if(handler.respondsTo('getHandlers'))
      return handler.getHandlers().findResult { findContextHandlerCollection(it) }
    return null
  }

  @Override
  void setHandlersToServer(server, List handlers) {
    ContextHandlerCollection contexts = findContextHandlerCollection(server.handler)
    if(!contexts)
      contexts = new ContextHandlerCollection()

    contexts.setHandlers(handlers as Handler[])
    if(server.handler == null)
      server.handler = contexts
  }

  @Override
  List getHandlersByContextPaths(Object server, List contextPaths) {
    ContextHandlerCollection context = findContextHandlerCollection(server.handler)
    return context.getHandlers().findAll {
      if(it.respondsTo("getContextPath")) {
        contextPaths.contains(it.getContextPath())
      }
    }
  }

  @Override
  void removeHandlerFromServer(server, handler) {
    ContextHandlerCollection collection = findContextHandlerCollection(server.handler)
    collection.removeHandler(handler)
  }

  @Override
  void addHandlerToServer(server, handler) {
    ContextHandlerCollection collection = findContextHandlerCollection(server.handler)
    collection.addHandler(handler)
  }
}

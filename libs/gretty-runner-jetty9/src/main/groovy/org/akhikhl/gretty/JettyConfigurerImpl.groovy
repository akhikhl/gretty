/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import ch.qos.logback.classic.selector.servlet.ContextDetachingSCL
import ch.qos.logback.classic.selector.servlet.LoggerContextFilter
import javax.servlet.DispatcherType
import org.eclipse.jetty.annotations.AnnotationConfiguration
import org.eclipse.jetty.plus.webapp.EnvConfiguration
import org.eclipse.jetty.plus.webapp.PlusConfiguration
import org.eclipse.jetty.security.HashLoginService
import org.eclipse.jetty.security.LoginService
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.NetworkConnector
import org.eclipse.jetty.server.SecureRequestCustomizer
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.SslConnectionFactory
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.server.session.HashSessionManager
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.eclipse.jetty.webapp.Configuration
import org.eclipse.jetty.webapp.FragmentConfiguration
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration
import org.eclipse.jetty.webapp.MetaInfConfiguration
import org.eclipse.jetty.webapp.WebAppClassLoader
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.webapp.WebInfConfiguration
import org.eclipse.jetty.webapp.WebXmlConfiguration
import org.eclipse.jetty.xml.XmlConfiguration
import org.slf4j.Logger

/**
 *
 * @author akhikhl
 */
class JettyConfigurerImpl implements JettyConfigurer {

  private Logger log
  private SSOAuthenticatorFactory ssoAuthenticatorFactory
  private HashSessionManager sharedSessionManager

  @Override
  void applyJettyEnvXml(webAppContext, String jettyEnvXml) {
    if(jettyEnvXml) {
      log.warn 'Configuring webAppContext with {}', jettyEnvXml
      XmlConfiguration xmlConfiguration = new XmlConfiguration(new File(jettyEnvXml).toURI().toURL())
      xmlConfiguration.configure(webAppContext)
    }
  }

  @Override
  void applyJettyXml(server, String jettyXml) {
    if(jettyXml != null) {
      log.warn 'Configuring server with {}', jettyXml
      XmlConfiguration xmlConfiguration = new XmlConfiguration(new File(jettyXml).toURI().toURL())
      xmlConfiguration.configure(server)
    }
  }

  @Override
  void configureConnectors(server, Map params) {

    HttpConfiguration http_config = new HttpConfiguration()
    if(params.httpsPort) {
      http_config.setSecureScheme('https')
      http_config.setSecurePort(params.httpsPort)
    }
    
    def connectors = server.getConnectors()
    
    Connector httpConn = connectors.find { it.protocols.contains('http') }

    if(params.httpEnabled) {
      boolean newConnector = false
      if(httpConn) {
        if(params.httpPort)
          httpConn.port = params.httpPort
      } else {
        newConnector = true
        httpConn = new ServerConnector(server, new HttpConnectionFactory(http_config))
        httpConn.port = params.httpPort ?: 8080
        httpConn.soLingerTime = -1
      }
      if(params.host)
        httpConn.host = params.host
      if(params.httpIdleTimeout)
        httpConn.idleTimeout = params.httpIdleTimeout
        
      if(newConnector)
        server.addConnector(httpConn)
    }
    
    Connector httpsConn = connectors.find { it.protocols.contains('https') }

    if(params.httpsEnabled) {
      boolean newConnector = false
      if(httpsConn) {
        if(params.httpsPort)
          httpsConn.port = params.httpsPort
      } else {
        newConnector = true
        HttpConfiguration https_config = new HttpConfiguration(http_config)
        https_config.addCustomizer(new SecureRequestCustomizer())

        httpsConn = new ServerConnector(server,
          new SslConnectionFactory(new SslContextFactory(), 'http/1.1'),
          new HttpConnectionFactory(https_config))
        
        httpsConn.port = params.httpsPort ?: 8443
      }
      
      def sslContextFactory = httpsConn.getConnectionFactories().find { it instanceof SslConnectionFactory }?.getSslContextFactory()
      if(sslContextFactory) {
        if(params.sslKeyStorePath)
          sslContextFactory.setKeyStorePath(params.sslKeyStorePath)
        if(params.sslKeyStorePassword)
          sslContextFactory.setKeyStorePassword(params.sslKeyStorePassword)
        if(params.sslKeyManagerPassword)
          sslContextFactory.setKeyManagerPassword(params.sslKeyManagerPassword)
        if(params.sslTrustStorePath)
          sslContextFactory.setTrustStorePath(params.sslTrustStorePath)
        if(params.sslTrustStorePassword)
          sslContextFactory.setTrustStorePassword(params.sslTrustStorePassword)
      }
      
      if(params.host)
        httpsConn.host = params.host
      if(params.httpsIdleTimeout)
        httpsConn.idleTimeout = params.httpsIdleTimeout
        
      if(newConnector)
        server.addConnector(httpsConn)
    }
  }

  @Override
  void configureSecurity(context, Map serverParams, Map webappParams) {
    String realm = webappParams.realm ?: serverParams.realm
    String realmConfigFile = webappParams.realmConfigFile ?: serverParams.realmConfigFile
    if(realm && realmConfigFile) {
      if(context.getSecurityHandler().getLoginService() != null)
        return
      log.warn '{} -> realm \'{}\', {}', context.contextPath, realm, realmConfigFile
      context.getSecurityHandler().setLoginService(new HashLoginService(realm, realmConfigFile))
      if(serverParams.singleSignOn) {
        if(ssoAuthenticatorFactory == null)
          ssoAuthenticatorFactory = new SSOAuthenticatorFactory()
        context.getSecurityHandler().setAuthenticatorFactory(ssoAuthenticatorFactory)
      }
    }
  }

  @Override
  void configureSessionManager(server, context, Map serverParams, Map webappParams) {
    HashSessionManager sessionManager
    if(serverParams.singleSignOn) {
      sessionManager = sharedSessionManager
      if(sessionManager == null) {
        sessionManager = sharedSessionManager = new HashSessionManager()
        sessionManager.setMaxInactiveInterval(60 * 30) // 30 minutes
        sessionManager.getSessionCookieConfig().setPath('/')
      }
    } else {
      sessionManager = new HashSessionManager()
      sessionManager.setMaxInactiveInterval(60 * 30) // 30 minutes
    }
    context.getSessionHandler().setSessionManager(sessionManager)
  }

  @Override
  def createServer() {
    // fix for issue https://github.com/akhikhl/gretty/issues/24
    org.eclipse.jetty.util.resource.Resource.defaultUseCaches = false
    return new Server()
  }

  @Override
  def createWebAppContext(List<String> webappClassPath) {
    WebAppContext context = new WebAppContext()
    context.setExtraClasspath(webappClassPath.collect { it.endsWith('.jar') ? it : (it.endsWith('/') ? it : it + '/') }.findAll { !(it =~ /.*javax\.servlet-api.*\.jar/) }.join(';'))
    context.addEventListener(new ContextDetachingSCL())
    context.addFilter(LoggerContextFilter.class, '/*', EnumSet.of(DispatcherType.REQUEST))
    return context
  }

  @Override
  List getConfigurations(List<String> webappClassPath) {
    [
      new WebInfConfiguration(),
      new WebXmlConfiguration(),
      new MetaInfConfiguration(),
      new FragmentConfiguration(),
      new EnvConfiguration(),
      new PlusConfiguration(),
      new AnnotationConfiguration(),
      new JettyWebXmlConfiguration()
    ]
  }

  @Override
  void setConfigurationsToWebAppContext(webAppContext, List configurations) {
    webAppContext.setConfigurations(configurations as Configuration[])
  }

  @Override
  void setHandlersToServer(server, List handlers) {
    ContextHandlerCollection contexts = new ContextHandlerCollection()
    contexts.setHandlers(handlers as Handler[])
    server.setHandler(contexts)
  }

  @Override
  void setLogger(Logger log) {
    this.log = log
  }
}

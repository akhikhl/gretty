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
import groovy.json.JsonSlurper
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

import javax.servlet.DispatcherType

final class Runner extends RunnerBase {

  static void main(String[] args) {
    assert args.length != 0
    Map params = new JsonSlurper().parseText(args[0])
    new Runner(params).run()
  }

  private Runner(Map params) {
    super(params)
  }

  @Override
  protected void addConfigurationClasses(webAppContext, List<String> webappClassPath) {
    webAppContext.setConfigurations([
      new WebInfConfiguration(),
      new WebXmlConfiguration(),
      new MetaInfConfiguration(),
      new FragmentConfiguration(),
      new EnvConfiguration(),
      new PlusConfiguration(),
      new AnnotationConfiguration(),
      new JettyWebXmlConfiguration()
    ] as Configuration[])
  }

  @Override
  protected void applyJettyEnvXml(webAppContext, jettyEnvXml) {
    if(jettyEnvXml) {
      log.warn 'Configuring webAppContext with {}', jettyEnvXml
      XmlConfiguration xmlConfiguration = new XmlConfiguration(new File(jettyEnvXml).toURI().toURL())
      xmlConfiguration.configure(webAppContext)
    }
  }

  @Override
  protected void applyJettyXml() {
    if(params.jettyXml != null) {
      log.warn 'Configuring server with {}', params.jettyXml
      XmlConfiguration xmlConfiguration = new XmlConfiguration(new File(params.jettyXml).toURI().toURL())
      xmlConfiguration.configure(server)
    }
  }

  @Override
  protected void configureConnectors() {
    if(server.getConnectors() != null && server.getConnectors().length != 0)
      return
    log.info 'Auto-configuring server connectors'

    HttpConfiguration http_config = new HttpConfiguration()
    if(params.httpsPort) {
      http_config.setSecureScheme('https')
      http_config.setSecurePort(params.httpsPort)
    }

    if(params.httpPort) {
      ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config))
      if(params.host)
        http.setHost(params.host)
      http.setPort(params.httpPort)
      if(params.httpIdleTimeout)
        http.setIdleTimeout(params.httpIdleTimeout)
      http.setSoLingerTime(-1)
      server.addConnector(http)
    }

    if(params.httpsPort) {
      SslContextFactory sslContextFactory = new SslContextFactory()
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

      HttpConfiguration https_config = new HttpConfiguration(http_config)
      https_config.addCustomizer(new SecureRequestCustomizer())

      ServerConnector https = new ServerConnector(server,
        new SslConnectionFactory(sslContextFactory, 'http/1.1'),
        new HttpConnectionFactory(https_config))
      if(params.host)
        https.setHost(params.host)
      https.setPort(params.httpsPort)
      if(params.httpsIdleTimeout)
        https.setIdleTimeout(params.httpsIdleTimeout)
      server.addConnector(https)
    }
  }

  @Override
  protected void configureRealm(context, String realm, String realmConfigFile) {
    if(realm && realmConfigFile) {
      if(context.getSecurityHandler().getLoginService() != null)
        return
      log.warn 'Configuring login service with realm \'{}\' and config {}', realm, realmConfigFile
      context.getSecurityHandler().setLoginService(new HashLoginService(realm, realmConfigFile))
    }
  }

  @Override
  protected createServer() {
    // fix for issue https://github.com/akhikhl/gretty/issues/24
    org.eclipse.jetty.util.resource.Resource.defaultUseCaches = false
    return new Server()
  }

  @Override
  protected createWebAppContext(List<String> webappClassPath) {
    WebAppContext context = new WebAppContext()
    context.setExtraClasspath(webappClassPath.collect { it.endsWith('.jar') ? it : (it.endsWith('/') ? it : it + '/') }.findAll { !(it =~ /.*javax\.servlet-api.*\.jar/) }.join(';'))
    context.addEventListener(new ContextDetachingSCL())
    context.addFilter(LoggerContextFilter.class, '/*', EnumSet.of(DispatcherType.REQUEST))
    return context
  }

  @Override
  protected void setHandlersToServer(List handlers) {
    ContextHandlerCollection contexts = new ContextHandlerCollection()
    contexts.setHandlers(handlers as Handler[])
    server.setHandler(contexts)
  }
}

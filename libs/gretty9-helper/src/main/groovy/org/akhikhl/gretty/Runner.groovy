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
import org.eclipse.jetty.security.HashLoginService
import org.eclipse.jetty.security.LoginService
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.NetworkConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.webapp.Configuration
import org.eclipse.jetty.webapp.WebAppClassLoader
import org.eclipse.jetty.webapp.WebAppContext
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

  protected void addConfigurationClassesToServer() {
    Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(server)
    classlist.addAfter('org.eclipse.jetty.webapp.FragmentConfiguration', 'org.eclipse.jetty.plus.webapp.EnvConfiguration', 'org.eclipse.jetty.plus.webapp.PlusConfiguration')
    classlist.addBefore('org.eclipse.jetty.webapp.JettyWebXmlConfiguration', 'org.eclipse.jetty.annotations.AnnotationConfiguration')
  }

  protected void applyContainerIncludeJarPattern(webAppContext) {
    if(!webAppContext.getAttribute('org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern'))
      webAppContext.setAttribute('org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern', '.*/classes/.*')
  }

  protected void applyJettyEnvXml(webAppContext) {
    if(params.jettyEnvXml != null) {
      System.out.println "Configuring webAppContext from ${params.jettyEnvXml}"
      XmlConfiguration xmlConfiguration = new XmlConfiguration(new File(params.jettyEnvXml).toURI().toURL())
      xmlConfiguration.configure(webAppContext)
    }
  }

  protected void applyJettyXml() {
    if(params.jettyXml != null) {
      System.out.println "Configuring server from ${params.jettyXml}"
      XmlConfiguration xmlConfiguration = new XmlConfiguration(new File(params.jettyXml).toURI().toURL())
      xmlConfiguration.configure(server)
    }
  }

  protected void configureConnectors() {
    if(server.getConnectors() != null && server.getConnectors().length != 0)
      return
    System.out.println 'Auto-configuring server connectors'
    ServerConnector conn = new ServerConnector(server)
    conn.setIdleTimeout(1000 * 60 * 60)
    conn.setSoLingerTime(-1)
    conn.setPort(params.port)
    conn.setConnectionFactories([ new HttpConnectionFactory() ])
    server.addConnector(conn)
  }

  protected void configureRealm(context) {
    if(context.getSecurityHandler().getLoginService() != null)
      return
    System.out.println 'Auto-configuring login service'
    Map realmInfo = params.realmInfo
    if(realmInfo?.realm && realmInfo?.realmConfigFile)
      context.getSecurityHandler().setLoginService(new HashLoginService(realmInfo.realm, realmInfo.realmConfigFile))
  }

  protected createServer() {
    return new Server()
  }

  protected createWebAppContext(ClassLoader classLoader) {
    WebAppContext context = new WebAppContext()
    context.setClassLoader(new WebAppClassLoader(classLoader, context))
    context.addEventListener(new ContextDetachingSCL())
    context.addFilter(LoggerContextFilter.class, '/*', EnumSet.of(DispatcherType.REQUEST))
    return context
  }

  protected int getServerPort() {
    if(server.getConnectors() != null)
      for(Connector conn in server.getConnectors())
        if(conn instanceof NetworkConnector)
          return conn.getLocalPort()
    return params.port
  }
}

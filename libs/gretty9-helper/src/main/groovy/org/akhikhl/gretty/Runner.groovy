/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonSlurper
import org.eclipse.jetty.security.HashLoginService
import org.eclipse.jetty.security.LoginService
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.webapp.WebAppClassLoader
import org.eclipse.jetty.webapp.WebAppContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class Runner extends RunnerBase {

  private static final Logger log = LoggerFactory.getLogger(Runner)

  static void main(String[] args) {
    if(args.length == 0) {
      log.error 'Arguments to Runner not specified'
      return
    }
    log.trace 'Runner args: {}', args
    Map params = new JsonSlurper().parseText(args[0])
    log.trace 'Runner params: {}', params
    new Runner(params).run()
  }

  private Runner(Map params) {
    super(params)
  }

  protected void configureConnectors() {
    ServerConnector conn = new ServerConnector(server)
    conn.setIdleTimeout(1000 * 60 * 60)
    conn.setSoLingerTime(-1)
    conn.setPort(params.port)
    conn.setConnectionFactories([ new HttpConnectionFactory() ])
    server.addConnector(conn)
  }

  protected void configureRealm(context) {
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
    return context
  }
}

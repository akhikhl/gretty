/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class RunnerBase {

  protected final Map params
  protected boolean paramsLoaded = false
  protected server

  protected Logger log

  RunnerBase(Map params) {
    this.params = params
  }

  protected void addConfigurationClasses(webAppContext, List<String> webappClassPath) {
  }

  protected abstract void applyJettyEnvXml(webAppContext, jettyEnvXml)

  protected abstract void applyJettyXml()

  protected abstract void configureConnectors()

  protected abstract void configureRealm(context, String realm, String realmConfigFile)

  protected abstract createServer()

  protected abstract createWebAppContext(List<String> webappClassPath)

  final void run() {
    try {
      ServerSocket socket = new ServerSocket(params.servicePort, 1, InetAddress.getByName('127.0.0.1'))
      try {
        ServiceProtocol.send(params.statusPort, 'init')
        while(true) {
          def data = ServiceProtocol.readMessageFromServerSocket(socket)
          if(!paramsLoaded) {
            params << new JsonSlurper().parseText(data)
            paramsLoaded = true
            startServer()
            ServiceProtocol.send(params.statusPort, 'started')
            // Note that server is already in listening state.
            // If client sends a command immediately after 'started' signal,
            // the command is queued, so that socket.accept gets it anyway.
            continue
          }
          if(data == 'stop') {
            stopServer()
            break
          }
          else if(data == 'restart') {
            stopServer()
            startServer()
          }
        }
      } finally {
        socket.close()
      }
    } catch(Exception e) {
      throw new RuntimeException(e)
    }
  }

  protected abstract void setHandlersToServer(List handlers)

  final void startServer() {
    assert server == null

    if(params.logging)
      LoggingUtils.configureLogging(params.logging)
    else if(params.logbackConfig)
      LoggingUtils.useConfig(params.logbackConfig)

    log = LoggerFactory.getLogger(this.getClass())

    server = createServer()
    applyJettyXml()
    configureConnectors()

    List handlers = []

    for(def webapp in params.webApps) {
      def context = createWebAppContext(webapp.webappClassPath)
      addConfigurationClasses(context, webapp.webappClassPath)
      applyJettyEnvXml(context, webapp.jettyEnvXml)
      configureRealm(context, webapp.realm, webapp.realmConfigFile)

      context.setContextPath(webapp.contextPath)

      webapp.initParams?.each { key, value ->
        context.setInitParameter(key, value)
      }

      if(webapp.resourceBase != null) {
        if(webapp.inplace)
          context.setResourceBase(webapp.resourceBase)
        else
          context.setWar(webapp.resourceBase)
      }

      handlers.add(context)
    }

    setHandlersToServer(handlers)

    server.start()
  }

  final void stopServer() {
    if(server != null) {
      server.stop()
      server = null
      log = null
    }
  }
}

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
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.webapp.WebAppClassLoader
import org.eclipse.jetty.webapp.WebAppContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Runner {

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

  private final Map params
  private Server server

  private Runner(Map params) {
    this.params = params
  }

  private void run() {

    startServer()

    Thread monitor = new MonitorThread(this)
    monitor.start()

    System.out.println 'Jetty server started.'
    System.out.println 'You can see web-application in browser under the address:'
    System.out.println "http://localhost:${params.port}${params.contextPath}"

    if(params.interactive)
      System.out.println 'Press any key to stop the jetty server.'
    else
      System.out.println 'Enter \'gradle jettyStop\' to stop the jetty server.'
    System.out.println()

    if(params.interactive) {
      System.in.read()
      if(monitor.running)
        ServiceControl.send(params.servicePort, 'stop')
    }

    monitor.join()

    System.out.println 'Jetty server stopped.'
  }

  void startServer() {
    assert server == null

    ClassLoader classLoader = new URLClassLoader(params.projectClassPath.collect { new URL(it) } as URL[], this.getClass().getClassLoader())

    server = new Server()

    SocketConnector connector = new SocketConnector()
    // Set some timeout options to make debugging easier.
    connector.setMaxIdleTime(1000 * 60 * 60)
    connector.setSoLingerTime(-1)
    connector.setPort(params.port)
    server.setConnectors([ connector ] as Connector[])

    WebAppContext context = new WebAppContext()
    context.setClassLoader(new WebAppClassLoader(classLoader, context))

    Map realmInfo = params.realmInfo
    if(realmInfo?.realm && realmInfo?.realmConfigFile)
      context.getSecurityHandler().setLoginService(new HashLoginService(realmInfo.realm, realmInfo.realmConfigFile))

    context.setContextPath(params.contextPath)

    params.initParams?.each { key, value ->
      context.setInitParameter(key, value)
    }

    if(params.inplace)
      context.setResourceBase(params.resourceBase)
    else
      context.setWar(params.resourceBase)

    context.setServer(server)
    server.setHandler(context)

    server.start()
  }

  void stopServer() {
    server.stop()
    server = null
  }
}

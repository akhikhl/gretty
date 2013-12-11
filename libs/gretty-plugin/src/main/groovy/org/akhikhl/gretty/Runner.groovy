/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class Runner {

  private static final Logger log = LoggerFactory.getLogger(Runner)

  final Project project
  final Map params
  def helper
  def server

  Runner(Map params, Project project) {
    this.project = project
    this.params = params
  }

  void consoleStart() {
    startServer()

    Thread monitor = new MonitorThread(this)
    monitor.start()

    project.gretty.onStart*.call()

    System.out.println 'Jetty server started.'
    System.out.println 'You can see web-application in browser under the address:'
    System.out.println "http://localhost:${project.gretty.port}${params.contextPath}"

    if(params.interactive)
      System.out.println 'Press any key to stop the jetty server.'
    else
      System.out.println 'Enter \'gradle jettyStop\' to stop the jetty server.'
    System.out.println()

    if(params.interactive) {
      System.in.read()
      if(monitor.running)
        ServiceControl.send(project.gretty.servicePort, 'stop')
    }

    monitor.join()

    System.out.println 'Jetty server stopped.'

    project.gretty.onStop*.call()
  }

  void startServer() {
    assert helper == null
    assert server == null

    ClassLoader classLoader = new URLClassLoader(params.classpath as URL[])

    helper = classLoader.findClass('org.akhikhl.gretty.GrettyHelper')

    server = helper.createServer()
    server.setConnectors helper.createConnectors(project.gretty.port)

    def context = helper.createWebAppContext()
    helper.setClassLoader(context, classLoader)

    ProjectUtils.RealmInfo realmInfo = params.realmInfo
    if(realmInfo.realm && realmInfo.realmConfigFile)
      context.getSecurityHandler().setLoginService(helper.createLoginService(realmInfo.realm, realmInfo.realmConfigFile))

    context.contextPath = params.contextPath

    params.initParams?.each { key, value ->
      context.setInitParameter key, value
    }

    if(params.inplace)
      context.setResourceBase "${project.buildDir}/inplaceWebapp"
    else
      context.setWar ProjectUtils.getFinalWarPath(project).toString()

    context.server = server
    server.handler = context

    server.start()
  }

  void stopServer() {
    server.stop()
    server = null
    helper = null
  }
}

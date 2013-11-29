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

final class GrettyRunner {

  public static void run(Map params = [:]) {

    Project project = params.project

    def urls = []

    urls.addAll project.configurations.grettyConfig.collect { it.toURI().toURL() }
    if(params.inplace) {
      urls.addAll project.sourceSets.main.runtimeClasspath.files.collect { it.toURI().toURL() }
      // ATTENTION: order of overlays is important!
      for(String overlay in project.gretty.overlays.reverse())
        urls.addAll project.project(overlay).sourceSets.main.runtimeClasspath.files.collect { it.toURI().toURL() }
    }
    ClassLoader classLoader = new URLClassLoader(urls as URL[])

    def helper = classLoader.findClass('org.akhikhl.gretty.GrettyHelper')

    def server = helper.createServer()
    server.setConnectors helper.createConnectors(project.gretty.port)

    def context = helper.createWebAppContext()
    helper.setClassLoader context, classLoader

    String realm = project.gretty.realm
    String realmConfigFile = project.gretty.realmConfigFile
    if(realmConfigFile && !new File(realmConfigFile).isAbsolute())
      realmConfigFile = "${project.webAppDir.absolutePath}/${realmConfigFile}"
    if(!realm || !realmConfigFile)
      for(def overlay in project.gretty.overlays.reverse()) {
        overlay = project.project(overlay)
        if(overlay.gretty.realm && overlay.gretty.realmConfigFile) {
          realm = overlay.gretty.realm
          realmConfigFile = overlay.gretty.realmConfigFile
          if(realmConfigFile && !new File(realmConfigFile).isAbsolute())
            realmConfigFile = "${overlay.webAppDir.absolutePath}/${realmConfigFile}"
          break
        }
      }
    if(realm && realmConfigFile)
      context.getSecurityHandler().setLoginService(helper.createLoginService(realm, realmConfigFile))

    String contextPath = project.gretty.contextPath
    if(!contextPath)
      for(def overlay in project.gretty.overlays.reverse()) {
        overlay = project.project(overlay)
        if(overlay.gretty.contextPath) {
          contextPath = overlay.gretty.contextPath
          break
        }
      }
    contextPath = contextPath ?: "/${project.name}"
    context.setContextPath contextPath

    for(def overlay in project.gretty.overlays) {
      overlay = project.project(overlay)
      for(def e in overlay.gretty.initParameters) {
        def paramValue = e.value
        if(paramValue instanceof Closure)
          paramValue = paramValue()
        context.setInitParameter e.key, paramValue
      }
    }
    for(def e in project.gretty.initParameters) {
      def paramValue = e.value
      if(paramValue instanceof Closure)
        paramValue = paramValue()
      context.setInitParameter e.key, paramValue
    }

    if(params.inplace)
      context.setResourceBase "${project.buildDir}/webapp"
    else
      context.setWar project.tasks.war.archivePath.toString()

    context.setServer server
    server.setHandler context

    server.start()

    boolean interactive = params.interactive

    System.out.println 'Jetty server started.'
    System.out.println 'You can see web-application in browser under the address:'
    System.out.println "http://localhost:${project.gretty.port}${contextPath}"
    for(def overlay in project.gretty.overlays) {
      overlay = project.project(overlay)
      overlay.gretty.onStart.each { onStart ->
        if(onStart instanceof Closure)
          onStart()
      }
    }
    project.gretty.onStart.each { onStart ->
      if(onStart instanceof Closure)
        onStart()
    }
    if(interactive)
      System.out.println 'Press any key to stop the jetty server.'
    else
      System.out.println 'Enter \'gradle jettyStop\' to stop the jetty server.'
    System.out.println()

    Thread monitor = new JettyMonitorThread(project.gretty.servicePort, server)
    monitor.start()

    if(interactive) {
      System.in.read()
      if(monitor.running)
        sendServiceCommand project.gretty.servicePort, 'stop'
    }

    server.join()

    System.out.println 'Jetty server stopped.'
    project.gretty.onStop.each { onStop ->
      if(onStop instanceof Closure)
        onStop()
    }
    for(def overlay in project.gretty.overlays.reverse()) {
      overlay = project.project(overlay)
      overlay.gretty.onStop.each { onStop ->
        if(onStop instanceof Closure)
          onStop()
      }
    }
  }

  public static void sendServiceCommand(int servicePort, String command) {
    Socket s = new Socket(InetAddress.getByName('127.0.0.1'), servicePort)
    try {
      OutputStream out = s.getOutputStream()
      System.out.println "Sending command: ${command}"
      out.write(("${command}\n").getBytes())
      out.flush()
    } finally {
      s.close()
    }
  }
}

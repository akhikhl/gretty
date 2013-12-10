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

  private static class RealmInfo {
    String realm
    String realmConfigFile
  }

  static void sendServiceCommand(int servicePort, String command) {
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

  final Project project
  final Map params
  def helper
  def server
  def scanner

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
    System.out.println "http://localhost:${project.gretty.port}${contextPath}"

    if(params.interactive)
      System.out.println 'Press any key to stop the jetty server.'
    else
      System.out.println 'Enter \'gradle jettyStop\' to stop the jetty server.'
    System.out.println()

    if(params.interactive) {
      System.in.read()
      if(monitor.running)
        sendServiceCommand project.gretty.servicePort, 'stop'
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

    RealmInfo realmInfo = getRealmInfo()
    if(realmInfo.realm && realmInfo.realmConfigFile)
      context.getSecurityHandler().setLoginService(helper.createLoginService(realmInfo.realm, realmInfo.realmConfigFile))

    String contextPath = getContextPath()
    context.contextPath = contextPath

    getInitParameters().each { key, value ->
      context.setInitParameter key, value
    }

    if(params.inplace)
      context.setResourceBase "${project.buildDir}/inplaceWebapp"
    else
      context.setWar ProjectUtils.getFinalWarPath(project).toString()

    context.server = server
    server.handler = context

    server.start()

    setupScanner()
  }

  void stopServer() {
    if(scanner != null) {
      log.info 'Stopping scanner'
      scanner.stop()
      scanner = null
    }
    server.stop()
    server = null
    helper = null
  }

  private String getContextPath() {
    String contextPath = project.gretty.contextPath
    if(!contextPath)
      for(def overlay in project.gretty.overlays.reverse()) {
        overlay = project.project(overlay)
        if(overlay.extensions.findByName('gretty')) {
          if(overlay.gretty.contextPath) {
            contextPath = overlay.gretty.contextPath
            break
          }
        } else
          log.warn 'Project {} is not gretty-enabled, could not extract it\'s context path', overlay
      }
    contextPath = contextPath ?: "/${project.name}"
    return contextPath
  }

  private Map getInitParameters() {
    Map initParams = [:]
    for(def overlay in project.gretty.overlays) {
      overlay = project.project(overlay)
      if(overlay.extensions.findByName('gretty')) {
        for(def e in overlay.gretty.initParameters) {
          def paramValue = e.value
          if(paramValue instanceof Closure)
            paramValue = paramValue()
          initParams[e.key] = paramValue
        }
      } else
        log.warn 'Project {} is not gretty-enabled, could not extract it\'s init parameters', overlay
    }
    for(def e in project.gretty.initParameters) {
      def paramValue = e.value
      if(paramValue instanceof Closure)
        paramValue = paramValue()
      initParams[e.key] = paramValue
    }
    return initParams
  }

  private RealmInfo getRealmInfo() {
    String realm = project.gretty.realm
    String realmConfigFile = project.gretty.realmConfigFile
    if(realmConfigFile && !new File(realmConfigFile).isAbsolute())
      realmConfigFile = "${project.webAppDir.absolutePath}/${realmConfigFile}"
    if(!realm || !realmConfigFile)
      for(def overlay in project.gretty.overlays.reverse()) {
        overlay = project.project(overlay)
        if(overlay.extensions.findByName('gretty')) {
          if(overlay.gretty.realm && overlay.gretty.realmConfigFile) {
            realm = overlay.gretty.realm
            realmConfigFile = overlay.gretty.realmConfigFile
            if(realmConfigFile && !new File(realmConfigFile).isAbsolute())
              realmConfigFile = "${overlay.webAppDir.absolutePath}/${realmConfigFile}"
            break
          }
        } else
          log.warn 'Project {} is not gretty-enabled, could not extract it\'s realm', overlay
      }
    return new RealmInfo(realm: realm, realmConfigFile: realmConfigFile)
  }

  private void setupScanner() {
    if(project.gretty.scanInterval == 0) {
      log.warn 'scanInterval not specified (or zero), scanning disabled'
      return
    }
    List<File> scanDirs = []
    if(params.inplace) {
      scanDirs.addAll project.sourceSets.main.runtimeClasspath.files
      scanDirs.add project.webAppDir
      for(def overlay in project.gretty.overlays) {
        overlay = project.project(overlay)
        scanDirs.addAll overlay.sourceSets.main.runtimeClasspath.files
        scanDirs.add overlay.webAppDir
      }
    } else {
      scanDirs.add project.tasks.war.archivePath
      scanDirs.add ProjectUtils.getFinalWarPath(project)
      for(def overlay in project.gretty.overlays)
        scanDirs.add ProjectUtils.getFinalWarPath(project.project(overlay))
    }
    for(def dir in project.gretty.scanDirs) {
      if(!(dir instanceof File))
        dir = project.file(dir.toString())
      scanDirs.add dir
    }
    for(File f in scanDirs)
      log.debug 'scanDir: {}', f
    scanner = helper.createScanner()
    scanner.reportDirs = true
    scanner.reportExistingFilesOnStartup = false
    scanner.scanInterval = project.gretty.scanInterval
    scanner.recursive = true
    scanner.scanDirs = scanDirs
    helper.addScannerScanCycleListener scanner, { started, cycle ->
      log.debug 'ScanCycleListener started={}, cycle={}', started, cycle
      project.gretty.onScan*.call()
    }
    helper.addScannerBulkListener scanner, { changedFiles ->
      log.debug 'BulkListener changedFiles={}', changedFiles
      project.gretty.onScanFilesChanged*.call(changedFiles)
      if(params.inplace)
        ProjectUtils.prepareInplaceWebAppFolder(project)
      else if(project.gretty.overlays) {
        ProjectUtils.prepareExplodedWebAppFolder(project)
        project.ant.zip destfile: project.ext.finalWarPath,  basedir: "${project.buildDir}/explodedWebapp"
      }
      else
        project.tasks.war.execute()
      sendServiceCommand(project.gretty.servicePort, 'restart')
    }
    log.info 'Starting scanner with interval of {} second(s)', project.gretty.scanInterval
    scanner.start()
  }
}

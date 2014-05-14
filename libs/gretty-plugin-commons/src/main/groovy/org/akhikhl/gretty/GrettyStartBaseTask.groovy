/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Base task for starting jetty
 *
 * @author akhikhl
 */
abstract class GrettyStartBaseTask extends GrettyBaseTask {

  protected static final Logger log = LoggerFactory.getLogger(GrettyStartBaseTask)

  boolean interactive = true
  boolean debug = false
  boolean integrationTest = false
  List<String> jvmArgs
  Integer port
  Integer servicePort
  Integer statusPort
  def jettyXmlFile
  Integer scanInterval // scan interval in seconds. When zero, scanning is disabled.
  String logbackConfigFile
  String loggingLevel
  Boolean consoleLogEnabled
  Boolean fileLogEnabled
  String logFileName
  String logDir
  List<Closure> onStart
  List<Closure> onStop
  List<Closure> onScan
  List<Closure> onScanFilesChanged
  ExecutorService executorService

  @Override
  void action() {
    Future futureStatus = ServiceControl.readMessage(executorService, statusPort)
    def runThread = Thread.start {
      runJetty()
    }
    def status = futureStatus.get()
    log.debug 'Got status: {}', status
    if(!integrationTest) {
      System.out.println 'Jetty server started.'
      List<WebAppConfig> webapps = getWebApps()
      if(webapps.size() == 1) {
        System.out.println 'Web-application runs at the address:'
        System.out.println "http://localhost:${port}${webapps[0].contextPath}"
      } else if(webapps.size() > 1) {
        System.out.println 'Web-applications run at the addresses:'
        for(def webapp in webapps)
          System.out.println "http://localhost:${port}${webapp.contextPath}"
      }
      if(interactive) {
        System.out.println 'Press any key to stop the jetty server.'
        System.in.read()
        log.debug 'Sending command: {}', 'stop'
        ServiceControl.send(servicePort, 'stop')
      } else
        System.out.println 'Run \'gradle jettyStop\' to stop the jetty server.'
      runThread.join()
      System.out.println 'Jetty server stopped.'
    }
  }

  protected File discoverLogbackConfigFile() {
    File result
    if(logbackConfigFile) {
      result = new File(logbackConfigFile)
      if(!result.isAbsolute()) {
        result = new File(project.projectDir, logbackConfigFile)
        if(!result.exists())
          result = ProjectUtils.findFileInOutput(project, logbackConfigFile)
      }
      if(!result || !result.exists())
        project.logger.warn 'The specified logback config file "{}" does not exist, ignoring', logbackConfigFile
      else
        project.logger.warn 'Using specified logback config file "{}"', logbackConfigFile
    } else {
      result = ProjectUtils.findFileInOutput(project, ~/logback\.(xml|groovy)/)
      if(result)
        project.logger.warn 'Using discovered logback config file "{}"', result
      else
        project.logger.warn 'Auto-configuring logback'
    }
    return result
  }

  protected abstract List<WebAppConfig> getWebApps()

  void onScan(Closure newValue) {
    if(onScan == null)
      onScan = []
    onScan.add newValue
  }

  void onScanFilesChanged(Closure newValue) {
    if(onScanFilesChanged == null)
      onScanFilesChanged = []
    onScanFilesChanged.add newValue
  }

  void onStart(Closure newValue) {
    if(onStart == null)
      onStart = []
    onStart.add newValue
  }

  void onStop(Closure newValue) {
    if(onStop == null)
      onStop = []
    onStop.add newValue
  }

  private static prepareJson(GrettyStartBaseTask task) {
    File logbackConfigFile = task.discoverLogbackConfigFile()
    def webAppsJson = []
    for(def webapp in task.getWebApps())
      webAppsJson.add {
        inplace webapp.inplace
        contextPath webapp.contextPath
        resourceBase (webapp.inplace ? webapp.inplaceResourceBase : webapp.warResourceBase)
        initParams webapp.initParameters
        realmInfo webapp.realmInfo
        jettyEnvXml webapp.jettyEnvXmlFile
        webappClassPath webapp.classPath
      }
    def json = new JsonBuilder()
    json {
      port task.port
      servicePort task.servicePort
      statusPort task.statusPort
      jettyXml task.jettyXmlFile
      if(logbackConfigFile)
        logbackConfig logbackConfigFile.absolutePath
      else
        logging {
          loggingLevel task.loggingLevel
          consoleLogEnabled task.consoleLogEnabled
          fileLogEnabled task.fileLogEnabled
          logFileName task.logFileName ?: task.project.name
          logDir task.logDir
        }
      webapps webAppsJson
    }
    return json
  }

  protected void runJetty() {

    onStart*.call()

    def json = prepareJson(this)
    project.logger.info json.toPrettyString()
    json = json.toString()

    // we are going to pass json as argument to java process.
    // under windows we must escape double quotes in process parameters.
    if(System.getProperty("os.name") =~ /(?i).*windows.*/)
      json = json.replace('"', '\\"')

    def thisTask = this
    ScannerManagerBase scanman = project.ext._createScannerManager()
    scanman.startScanner(this, inplace)
    try {
      project.javaexec { spec ->
        spec.classpath = project.configurations.gretty
        spec.main = 'org.akhikhl.gretty.Runner'
        spec.args = [json]
        spec.jvmArgs = thisTask.jvmArgs
        spec.debug = thisTask.debug
      }
    } finally {
      scanman.stopScanner()
    }

    onStop*.call()
  }

  @Override
  protected void setupProperties() {
    if(jvmArgs == null) jvmArgs = project.gretty.jvmArgs
    if(port == null) port = project.gretty.port
    if(servicePort == null) servicePort = project.gretty.servicePort
    if(statusPort == null) statusPort = project.gretty.statusPort
    if(jettyXmlFile == null)
      jettyXmlFile = ProjectUtils.getJettyXmlFile(project)
    else
      jettyXmlFile = ProjectUtils.resolveSingleFile(project, jettyXmlFile)
    if(scanInterval == null) scanInterval = project.gretty.scanInterval
    if(logbackConfigFile == null) logbackConfigFile = project.gretty.logbackConfigFile
    if(loggingLevel == null) loggingLevel = project.gretty.loggingLevel
    if(consoleLogEnabled == null) consoleLogEnabled = project.gretty.consoleLogEnabled
    if(fileLogEnabled == null) fileLogEnabled = project.gretty.fileLogEnabled
    if(logFileName == null) logFileName = project.gretty.logFileName
    if(logDir == null) logDir = project.gretty.logDir
    if(onStart == null) onStart = project.gretty.onStart
    if(onStop == null) onStop = project.gretty.onStop
    if(onScan == null) onScan = project.gretty.onScan
    if(onScanFilesChanged == null) onScanFilesChanged = project.gretty.onScanFilesChanged
    if(executorService == null) executorService = project.ext.executorService ?: Executors.newSingleThreadExecutor()
    for(def webapp in getWebApps())
      webapp.setupProperties(project)
  }
}

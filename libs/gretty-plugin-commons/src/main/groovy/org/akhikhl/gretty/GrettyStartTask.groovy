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
import org.gradle.api.DefaultTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Gradle task for starting jetty
 *
 * @author akhikhl
 */
class GrettyStartTask extends GrettyBaseTask {

  protected static final Logger log = LoggerFactory.getLogger(GrettyStartTask)

  boolean inplace = true
  boolean interactive = true
  boolean debug = false
  boolean integrationTest = false
  Integer port
  Integer servicePort
  Integer statusPort
  String contextPath
  Map initParameters
  String realm
  def realmConfigFile
  def jettyXmlFile
  def jettyEnvXmlFile
  List<Closure> onStart
  List<Closure> onStop
  Integer scanInterval // scan interval in seconds. When zero, scanning is disabled.
  List scanDirs // list of additional scan directories
  List<Closure> onScan
  List<Closure> onScanFilesChanged
  def fastReload
  String logbackConfigFile
  String loggingLevel
  Boolean consoleLogEnabled
  Boolean fileLogEnabled
  String logFileName
  String logDir
  List<String> jvmArgs
  Collection<URL> classPath
  String inplaceResourceBase
  String warResourceBase
  ExecutorService executorService

  protected RealmInfo realmInfo

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
      System.out.println 'You can see web-application in browser under the address:'
      System.out.println "http://localhost:${port}${contextPath}"
      if(interactive) {
        System.out.println 'Press any key to stop the jetty server.'
        System.in.read()
        log.debug 'Sending command: {}', 'stop'
        ServiceControl.send(servicePort, 'stop')
      } else
        System.out.println 'Run \'gradle jettyStop\' to stop the jetty server.'
      runThread.join()
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

  void fastReload(String arg) {
    if(fastReload == null)
      fastReload = []
    fastReload.add(arg)
  }

  void fastReload(File arg) {
    if(fastReload == null)
      fastReload = []
    fastReload.add(arg)
  }

  void fastReload(Map map) {
    if(fastReload == null)
      fastReload = []
    fastReload.add(map)
  }

  void initParameter(key, value) {
    if(initParameters == null)
      initParameters = [:]
    initParameters[key] = value
  }

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

  private static prepareJson(GrettyStartTask task) {
    File logbackConfigFile = task.discoverLogbackConfigFile()
    def json = new JsonBuilder()
    json {
      projectName task.project.name
      inplace task.inplace
      port task.port
      servicePort task.servicePort
      statusPort task.statusPort
      contextPath task.contextPath
      resourceBase (task.inplace ? task.inplaceResourceBase : task.warResourceBase)
      initParams task.initParameters
      realmInfo task.realmInfo
      jettyXml task.jettyXmlFile
      jettyEnvXml task.jettyEnvXmlFile
      projectClassPath task.classPath
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

  void scanDir(String value) {
    if(scanDirs == null)
      scanDirs = []
    scanDirs.add(new File(value))
  }

  void scanDir(File value) {
    if(scanDirs == null)
      scanDirs = []
    scanDirs.add(value)
  }

  void scanDir(Object[] args) {
    for(def arg in args)
      if(arg != null) {
        if(scanDirs == null)
          scanDirs = []
        scanDirs.add(arg)
      }
  }

  void setFastReload(boolean newValue) {
    fastReload = [ newValue ]
  }

  @Override
  protected void setupProperties() {
    if(port == null) port = project.gretty.port
    if(servicePort == null) servicePort = project.gretty.servicePort
    if(statusPort == null) statusPort = project.gretty.statusPort
    if(contextPath == null) {
      contextPath = project.gretty.contextPath
      if(contextPath == null)
        contextPath = '/' + project.name
      else if(!contextPath.startsWith('/'))
        contextPath = '/' + contextPath
    }
    if(initParameters == null) initParameters = ProjectUtils.getInitParameters(project)
    if(!realm || !realmConfigFile)
      realmInfo = ProjectUtils.getRealmInfo(project)
    else {
      if(!(realmConfigFile instanceof File))
        realmConfigFile = new File(realmConfigFile)
      if(!realmConfigFile.isAbsolute())
        realmConfigFile = new File(project.webAppDir, realmConfigFile.path)
      realmInfo = new RealmInfo(realm: realm, realmConfigFile: realmConfigFile.absolutePath)
    }
    if(jettyXmlFile == null)
      jettyXmlFile = ProjectUtils.getJettyXmlFile(project)
    else
      jettyXmlFile = ProjectUtils.resolveSingleFile(project, jettyXmlFile)
    if(jettyEnvXmlFile == null)
      jettyEnvXmlFile = ProjectUtils.getJettyEnvXmlFile(project)
    else
      jettyEnvXmlFile = ProjectUtils.resolveSingleFile(project, jettyEnvXmlFile)
    if(onStart == null) onStart = project.gretty.onStart
    if(onStop == null) onStop = project.gretty.onStop
    if(onScan == null) onScan = project.gretty.onScan
    if(onScanFilesChanged == null) onScanFilesChanged = project.gretty.onScanFilesChanged
    if(scanInterval == null) scanInterval = project.gretty.scanInterval
    if(scanDirs == null) scanDirs = project.gretty.scanDirs
    if(logbackConfigFile == null) logbackConfigFile = project.gretty.logbackConfigFile
    if(loggingLevel == null) loggingLevel = project.gretty.loggingLevel
    if(consoleLogEnabled == null) consoleLogEnabled = project.gretty.consoleLogEnabled
    if(fileLogEnabled == null) fileLogEnabled = project.gretty.fileLogEnabled
    if(logFileName == null) logFileName = project.gretty.logFileName
    if(logDir == null) logDir = project.gretty.logDir
    if(jvmArgs == null) jvmArgs = project.gretty.jvmArgs
    if(classPath == null) classPath = ProjectUtils.getClassPath(project, inplace)
    if(inplaceResourceBase == null) inplaceResourceBase = "${project.buildDir}/inplaceWebapp"
    if(warResourceBase == null) warResourceBase = ProjectUtils.getFinalWarPath(project).toString()
    if(executorService == null) executorService = project.ext.executorService ?: Executors.newSingleThreadExecutor()
  }
}

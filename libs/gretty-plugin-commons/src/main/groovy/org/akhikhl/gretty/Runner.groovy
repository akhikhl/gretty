/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonBuilder
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
final class Runner {

  protected static final Logger log = LoggerFactory.getLogger(Runner)

  protected final StartBaseTask startTask
  protected final Project project
  protected ServerConfig sconfig
  protected Iterable<WebAppConfig> webAppConfigs
  protected final ExecutorService executorService

  Runner(StartBaseTask startTask) {
    this.startTask = startTask
    project = startTask.project
    RunConfig runConfig = startTask.getRunConfig()
    sconfig = runConfig.getServerConfig()
    webAppConfigs = runConfig.getWebAppConfigs()
    executorService = Executors.newSingleThreadExecutor()
  }

  private getCommandLineJson() {
    def json = new JsonBuilder()
    json {
      servicePort sconfig.servicePort
      statusPort sconfig.statusPort
    }
    json
  }

  private getRunConfigJson() {
    def json = new JsonBuilder()
    json {
      port sconfig.port
      if(sconfig.jettyXmlFile)
        jettyXml sconfig.jettyXmlFile.absolutePath
      if(sconfig.logbackConfigFile)
        logbackConfig sconfig.logbackConfigFile.absolutePath
      else
        logging {
          loggingLevel sconfig.loggingLevel
          consoleLogEnabled sconfig.consoleLogEnabled
          fileLogEnabled sconfig.fileLogEnabled
          logFileName sconfig.logFileName
          logDir sconfig.logDir
        }
      webApps webAppConfigs.collect { WebAppConfig webAppConfig ->
        { ->
          inplace webAppConfig.inplace
          webappClassPath webAppConfig.classPath
          contextPath webAppConfig.contextPath
          resourceBase (webAppConfig.inplace ? webAppConfig.inplaceResourceBase : webAppConfig.warResourceBase ?: webAppConfig.warResourceBase.toString() ?: '')
          if(webAppConfig.initParameters)
            initParams webAppConfig.initParameters
          if(webAppConfig.realm && webAppConfig.realmConfigFile) {
            realm webAppConfig.realm
            realmConfigFile webAppConfig.realmConfigFile.absolutePath
          }
          if(webAppConfig.jettyEnvXmlFile)
            jettyEnvXml webAppConfig.jettyEnvXmlFile.absolutePath
        }
      }
    }
    json
  }

  void run() {
    for(WebAppConfig webAppConfig in webAppConfigs)
      webAppConfig.prepareToRun()

    Future futureStatus = executorService.submit({ ServiceProtocol.readMessage(sconfig.statusPort) } as Callable)
    def runThread = Thread.start {
      runJetty()
    }
    def status = futureStatus.get()
    log.debug 'Got init status: {}', status

    futureStatus = executorService.submit({ ServiceProtocol.readMessage(sconfig.statusPort) } as Callable)
    def runConfigJson = getRunConfigJson()
    log.debug 'Sending parameters to port {}', sconfig.servicePort
    log.debug runConfigJson.toPrettyString()
    ServiceProtocol.send(sconfig.servicePort, runConfigJson.toString())
    status = futureStatus.get()
    log.debug 'Got start status: {}', status

    System.out.println "Jetty server ${project.ext.jettyVersion} started."
    for(WebAppConfig webAppConfig in webAppConfigs) {
      String webappName
      if(webAppConfig.inplace)
        webappName = webAppConfig.projectPath
      else {
        def warFile = webAppConfig.warResourceBase
        if(!(warFile instanceof File))
          warFile = new File(warFile.toString())
        webappName = warFile.name
      }
      System.out.println "${webappName} runs at the address http://localhost:${sconfig.port}${webAppConfig.contextPath}"
    }
    System.out.println "servicePort: ${sconfig.servicePort}, statusPort: ${sconfig.statusPort}"

    if(startTask.getIntegrationTest())
      project.ext.grettyRunnerThread = runThread
    else {
      if(startTask.interactive) {
        System.out.println 'Press any key to stop the jetty server.'
        System.in.read()
        log.debug 'Sending command: {}', 'stop'
        ServiceProtocol.send(sconfig.servicePort, 'stop')
      } else
        System.out.println "Run 'gradle ${startTask.getStopTaskName()}' to stop the jetty server."
      runThread.join()
      System.out.println 'Jetty server stopped.'
    }
  }

  protected void runJetty() {

    sconfig.onStart*.call()

    def cmdLineJson = getCommandLineJson()
    log.debug 'Command-line json: {}', cmdLineJson.toPrettyString()
    cmdLineJson = cmdLineJson.toString()

    // we are going to pass json as argument to java process.
    // under windows we must escape double quotes in process parameters.
    if(System.getProperty("os.name") =~ /(?i).*windows.*/)
      cmdLineJson = cmdLineJson.replace('"', '\\"')

    ScannerManagerBase scanman = project.ext.scannerManagerFactory.createScannerManager()
    scanman.startScanner(project, sconfig, webAppConfigs)
    Runner self = this
    try {
      project.javaexec { spec ->
        spec.classpath = project.configurations.gretty
        spec.main = 'org.akhikhl.gretty.Runner'
        spec.args = [ cmdLineJson ]
        spec.debug = startTask.debug
        spec.jvmArgs sconfig.jvmArgs
        if(startTask.jacoco) {
          String jarg = startTask.jacoco.getAsJvmArg()
          log.debug 'jacoco jvmArgs: {}', jarg
          spec.jvmArgs jarg
        }
      }
    } finally {
      scanman.stopScanner()
    }

    sconfig.onStop*.call()
  }
}

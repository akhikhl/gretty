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

  protected final Project project
  protected final ServerConfig sconfig
  protected final Iterable<WebAppConfig> webAppConfigs
  protected final boolean interactive
  protected final boolean debug
  protected final boolean integrationTest
  private ExecutorService executorService
  private String stopTask

  Runner(Project project, RunConfig runConfig, boolean interactive, boolean debug, boolean integrationTest, String stopTask) {
    this.project = project
    this.sconfig = runConfig.getServerConfig()
    this.webAppConfigs = runConfig.getWebAppConfigs()
    this.interactive = interactive
    this.debug = debug
    this.integrationTest = integrationTest
    this.stopTask = stopTask
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
          resourceBase (webAppConfig.inplace ? webAppConfig.inplaceResourceBase : webAppConfig.warResourceBase)
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
    log.warn 'Got init status: {}', status

    futureStatus = executorService.submit({ ServiceProtocol.readMessage(sconfig.statusPort) } as Callable)
    def runConfigJson = getRunConfigJson()
    log.warn 'Sending parameters to port {}', sconfig.servicePort
    log.warn runConfigJson.toPrettyString()
    ServiceProtocol.send(sconfig.servicePort, runConfigJson.toString())
    status = futureStatus.get()
    log.warn 'Got start status: {}', status

    System.out.println "Jetty server ${project.ext.jettyVersion} started."
    for(WebAppConfig webAppConfig in webAppConfigs) {
      String webappName = webAppConfig.inplace ? webAppConfig.projectPath : new File(webAppConfig.warResourceBase).name
      System.out.println "${webappName} runs at the address http://localhost:${sconfig.port}${webAppConfig.contextPath}"
    }
    System.out.println "servicePort: ${sconfig.servicePort}, statusPort: ${sconfig.statusPort}"

    if(integrationTest)
      project.ext.grettyRunnerThread = runThread
    else {
      if(interactive) {
        System.out.println 'Press any key to stop the jetty server.'
        System.in.read()
        log.debug 'Sending command: {}', 'stop'
        ServiceProtocol.send(sconfig.servicePort, 'stop')
      } else
        System.out.println "Run 'gradle ${stopTask}' to stop the jetty server."
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
        spec.jvmArgs = sconfig.jvmArgs
        spec.debug = self.debug
      }
    } finally {
      scanman.stopScanner()
    }

    sconfig.onStop*.call()
  }
}

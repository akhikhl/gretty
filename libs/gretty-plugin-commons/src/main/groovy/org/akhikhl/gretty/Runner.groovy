/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonBuilder
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
  protected final List<WebAppConfig> webapps
  protected final boolean interactive
  protected final boolean debug
  protected final boolean integrationTest
  private ExecutorService executorService
  private String stopTask

  Runner(Project project, ServerConfig sconfig, List<WebAppConfig> webapps, boolean interactive, boolean debug, boolean integrationTest, String stopTask) {
    this.project = project
    this.sconfig = sconfig
    this.webapps = webapps
    this.interactive = interactive
    this.debug = debug
    this.integrationTest = integrationTest
    this.stopTask = stopTask
    executorService = Executors.newSingleThreadExecutor()
  }

  void run() {
    Future futureStatus = ServiceControl.readMessage(executorService, sconfig.statusPort)
    def runThread = Thread.start {
      runJetty()
    }
    def status = futureStatus.get()
    log.debug 'Got status: {}', status
    if(!integrationTest) {
      System.out.println "Jetty server ${project.ext.jettyVersion} started."
      for(WebAppConfig webapp in webapps) {
        String webappName = webapp.inplace ? webapp.projectPath : new File(webapp.warResourceBase).name
        System.out.println "${webappName} runs at the address http://localhost:${sconfig.port}${webapp.contextPath}"
      }
      System.out.println "servicePort: ${sconfig.servicePort}, statusPort: ${sconfig.statusPort}"
      if(interactive) {
        System.out.println 'Press any key to stop the jetty server.'
        System.in.read()
        log.debug 'Sending command: {}', 'stop'
        ServiceControl.send(sconfig.servicePort, 'stop')
      } else
        System.out.println "Run 'gradle ${stopTask}' to stop the jetty server."
      runThread.join()
      System.out.println 'Jetty server stopped.'
    }
  }

  private prepareJson() {
    def json = new JsonBuilder()
    json {
      port sconfig.port
      servicePort sconfig.servicePort
      statusPort sconfig.statusPort
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
      webApps webapps.collect { WebAppConfig webapp ->
        { ->
          inplace webapp.inplace
          webappClassPath webapp.classPath
          contextPath webapp.contextPath
          resourceBase (webapp.inplace ? webapp.inplaceResourceBase : webapp.warResourceBase)
          if(webapp.initParameters)
            initParams webapp.initParameters
          if(webapp.realm && webapp.realmConfigFile) {
            realm webapp.realm
            realmConfigFile webapp.realmConfigFile.absolutePath
          }
          if(webapp.jettyEnvXmlFile)
            jettyEnvXml webapp.jettyEnvXmlFile.absolutePath
        }
      }
    }
    return json
  }

  protected void runJetty() {

    sconfig.onStart*.call()

    def json = prepareJson()
    log.debug json.toPrettyString()
    json = json.toString()

    // we are going to pass json as argument to java process.
    // under windows we must escape double quotes in process parameters.
    if(System.getProperty("os.name") =~ /(?i).*windows.*/)
      json = json.replace('"', '\\"')

    ScannerManagerBase scanman = project.ext.scannerManagerFactory.createScannerManager()
    scanman.startScanner(project, sconfig, webapps)
    Runner self = this
    try {
      project.javaexec { spec ->
        spec.classpath = project.configurations.gretty
        spec.main = 'org.akhikhl.gretty.Runner'
        spec.args = [ json ]
        spec.jvmArgs = sconfig.jvmArgs
        spec.debug = self.debug
      }
    } finally {
      scanman.stopScanner()
    }

    sconfig.onStop*.call()
  }
}

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonBuilder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
final class Runner {

  protected static final Logger log = LoggerFactory.getLogger(Runner)

  protected final ServerConfig sconfig
  protected final List<WebAppRunConfig> webapps
  protected final ExecutorService executorService
  protected final boolean interactive
  protected final boolean debug
  protected final boolean integrationTest

  Runner(ServerConfig sconfig, List<WebAppRunConfig> webapps, ExecutorService executorService, boolean interactive, boolean debug, boolean integrationTest) {
    this.sconfig = sconfig
    this.webapps = webapps
    this.executorService = executorService
    this.interactive = interactive
    this.debug = debug
    this.integrationTest = integrationTest
  }

  void run() {
    Future futureStatus = ServiceControl.readMessage(executorService, sconfig.statusPort)
    def runThread = Thread.start {
      runJetty()
    }
    def status = futureStatus.get()
    log.debug 'Got status: {}', status
    if(!integrationTest) {
      System.out.println 'Jetty server started.'
      if(webapps.size() == 1) {
        System.out.println 'Web-application runs at the address:'
        System.out.println "http://localhost:${sconfig.port}${webapps[0].contextPath}"
      } else if(webapps.size() > 1) {
        System.out.println 'Web-applications run at the addresses:'
        for(WebAppRunConfig webapp in webapps)
          System.out.println "http://localhost:${sconfig.port}${webapp.contextPath}"
      }
      if(interactive) {
        System.out.println 'Press any key to stop the jetty server.'
        System.in.read()
        log.debug 'Sending command: {}', 'stop'
        ServiceControl.send(sconfig.servicePort, 'stop')
      } else
        System.out.println 'Run \'gradle jettyStop\' to stop the jetty server.'
      runThread.join()
      System.out.println 'Jetty server stopped.'
    }
  }

  private prepareJson() {
    File logbackConfigFile = discoverLogbackConfigFile()
    def webAppsJson = []
    for(WebAppRunConfig webapp in webapps)
      webAppsJson.add {
        inplace webapp.inplace
        webappClassPath webapp.classPath
        contextPath webapp.contextPath
        resourceBase webapp.resourceBase
        initParams webapp.initParameters
        if(webapp.realm && webapp.realmConfigFile) {
          realm webapp.realm
          realmConfigFile webapp.realmConfigFile.absolutePath
        }
        if(webapp.jettyEnvXmlFile)
          jettyEnvXml webapp.jettyEnvXmlFile.absolutePath
      }
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
      webapps webAppsJson
    }
    return json
  }

  protected void runJetty() {

    sconfig.onStart*.call()

    def json = prepareJson()
    log.info json.toPrettyString()
    json = json.toString()

    // we are going to pass json as argument to java process.
    // under windows we must escape double quotes in process parameters.
    if(System.getProperty("os.name") =~ /(?i).*windows.*/)
      json = json.replace('"', '\\"')

    ScannerManagerBase scanman = project.ext._createScannerManager()
    scanman.startScanner(this, inplace)
    try {
      project.javaexec { spec ->
        spec.classpath = project.configurations.gretty
        spec.main = 'org.akhikhl.gretty.Runner'
        spec.args = [json]
        spec.jvmArgs = sconfig.jvmArgs
        spec.debug = sconfig.debug
      }
    } finally {
      scanman.stopScanner()
    }

    sconfig.onStop*.call()
  }
}


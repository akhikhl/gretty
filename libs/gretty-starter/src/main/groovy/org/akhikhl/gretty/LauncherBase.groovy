/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
abstract class LauncherBase implements Launcher {

  protected static final Logger log = LoggerFactory.getLogger(LauncherBase)

  protected final LauncherConfig config
  protected final ServerConfig sconfig
  protected final Iterable<WebAppConfig> webAppConfigs
  protected serverStartInfo

  ScannerManager scannerManager

  LauncherBase(LauncherConfig config) {
    this.config = config
    sconfig = config.getServerConfig()
    webAppConfigs = config.getWebAppConfigs()
  }

  protected static fileToString(file) {
    file instanceof File ? file.absolutePath : file.toString()
  }

  private getRunConfigJson() {
    def json = new JsonBuilder()
    json {
      writeRunConfigJson(delegate)
    }
    json
  }

  protected String getServerManagerFactory() {
    'org.akhikhl.gretty.ServerManagerFactory'
  }

  protected abstract String getServletContainerId()

  protected abstract String getServletContainerDescription()

  def getServerStartInfo() {
    serverStartInfo
  }

  protected abstract void javaExec(JavaExecParams params)

  protected abstract void rebuildWebapps()

  @Override
  void launch() {
    Thread thread = launchThread()
    def stopServer = {
      log.debug 'Sending command: {}', 'stop'
      ServiceProtocol.send(sconfig.servicePort, 'stop')
    }
    if(config.getInteractive()) {
      if(sconfig.interactiveMode == 'restartOnKeyPress' || sconfig.interactiveMode == 'rebuildAndRestartOnKeyPress') {
        def hint = 'Press \'q\' or \'Q\' to stop the server or any other key to restart.'
        System.out.println hint
        ExecutorService executorService = Executors.newSingleThreadExecutor()
          try {
            // Based on Jetty-Maven-Plugin console scanner
            infinite:
            while(true) {
              while(System.in.available() > 0) {
                def input = System.in.read()
                if(input >= 0) {
                  char c = input as char
                  if(c == 'q' || c == 'Q') {
                    stopServer()
                    break infinite
                  } else {
                    if(sconfig.interactiveMode == 'rebuildAndRestartOnKeyPress') {
                      rebuildWebapps()
                    }
                    log.debug 'Sending command: {}', 'restartWithEvent'
                    def futureStatus = executorService.submit({
                      ServiceProtocol.readMessage(sconfig.statusPort)
                    } as Callable)
                    ServiceProtocol.send(sconfig.servicePort, 'restartWithEvent')
                    // Waiting for restart complete event
                    def status = futureStatus.get()
                    log.debug "Received status: ${status}"
                    //
                    System.out.println hint
                    // dumping input
                    while (System.in.available() > 0) {
                      long available = System.in.available()
                      for (int i = 0; i < available; i++) {
                        // Stream ended suddenly
                        if (System.in.read() == -1) {
                          break
                        }
                      }
                    }
                  }
                }
              }
              // Sleep to prevent busy wait
              Thread.sleep(500)
            }
          } finally {
              executorService.shutdown()
          }
      } else if(sconfig.interactiveMode == 'stopOnKeyPress') {
          System.out.println 'Press any key to stop the server.'
          System.in.read()
          stopServer()
      } else {
        log.warn 'Unexpected interactiveMode: {}', sconfig.interactiveMode
      }
    } else
      System.out.println "Run '${config.getStopCommand()}' to stop the server."
    thread.join()
  }

  @Override
  Thread launchThread() {

    for(WebAppConfig wconfig in webAppConfigs)
      prepareToRun(wconfig)

    log.debug 'servicePort: {}, statusPort: {}', sconfig.servicePort, sconfig.statusPort

    Thread thread
    ExecutorService executorService = Executors.newSingleThreadExecutor()
    try {
      def listeningForStatusLock = new Object()
      boolean listeningForStatus

      def asyncReadStatus = {
        listeningForStatus = false
        def future = executorService.submit({
          synchronized(listeningForStatusLock) {
            listeningForStatus = true
          }
          ServiceProtocol.readMessage(sconfig.statusPort)
        } as Callable)
        while(true) {
          synchronized(listeningForStatusLock) {
            if(listeningForStatus)
              break
          }
          Thread.sleep(100)
        }
        future
      }

      Future futureStatus = asyncReadStatus()

      def handleConnectionError = { e ->
        log.debug 'Sending "notStarted" to status port...'
        ServiceProtocol.send(sconfig.statusPort, 'notStarted')
      }

      try {
        log.debug 'Sending "status" command to (probably) running server...'
        ServiceProtocol.send(sconfig.servicePort, 'status')
      } catch(java.net.ConnectException e) {
        handleConnectionError(e)
      } catch(java.net.SocketException e) {
        handleConnectionError(e)
      }

      log.debug 'Reading response...'
      def status = futureStatus.get()
      log.debug 'Got response: {}', status

      if(status == 'started')
        throw new RuntimeException('Web-server is already running.')

      futureStatus = asyncReadStatus()
      thread = Thread.start {
        for(Closure c in sconfig.onStart) {
          c.delegate = sconfig
          c.resolveStrategy = Closure.DELEGATE_FIRST
          c()
        }
        try {
          scannerManager?.startScanner()
          try {
            JavaExecParams params = new JavaExecParams()
            params.main = 'org.akhikhl.gretty.Runner'
            params.args = [ "--servicePort=${sconfig.servicePort}", "--statusPort=${sconfig.statusPort}", "--serverManagerFactory=${getServerManagerFactory()}" ]
            params.debug = config.getDebug()
            params.debugSuspend = config.getDebugSuspend()
            params.debugPort = config.getDebugPort()
            params.jvmArgs = sconfig.jvmArgs
            params.systemProperties = sconfig.systemProperties
            if(!sconfig.secureRandom) {
              // Speeding up tomcat startup, according to http://wiki.apache.org/tomcat/HowTo/FasterStartUp
              // ATTENTION: replacing the blocking entropy source (/dev/random) with a non-blocking one
              // actually reduces security because you are getting less-random data.
              params.systemProperty 'java.security.egd', 'file:/dev/./urandom'
            }
            javaExec(params)
          } finally {
            scannerManager?.stopScanner()
          }
        } finally {
          for(Closure c in sconfig.onStop) {
            c.delegate = sconfig
            c.resolveStrategy = Closure.DELEGATE_FIRST
            c()
          }
        }
      }

      log.debug 'Reading response...'
      status = futureStatus.get()
      log.debug 'Got response: {}', status

      futureStatus = asyncReadStatus()
      def runConfigJson = getRunConfigJson()
      log.debug 'Sending parameters to port {}', sconfig.servicePort
      log.debug runConfigJson.toPrettyString()
      ServiceProtocol.send(sconfig.servicePort, runConfigJson.toString())
      status = futureStatus.get()
      log.debug 'Got start status: {}', status
      serverStartInfo = new JsonSlurper().parseText(status)

    } finally {
      executorService.shutdown()
    }

    thread
  }

  protected void prepareToRun(WebAppConfig wconfig) {
    wconfig.prepareToRun()
  }

  protected void writeLoggingConfig(json) {
    json.with {
      if(sconfig.logbackConfigFile)
        logbackConfigFile sconfig.logbackConfigFile.toString()
      loggingLevel sconfig.loggingLevel
      consoleLogEnabled sconfig.consoleLogEnabled
      fileLogEnabled sconfig.fileLogEnabled
      if(sconfig.logFileName)
        logFileName sconfig.logFileName
      if(sconfig.logDir)
        logDir sconfig.logDir.toString()
    }
  }

  protected void writeRunConfigJson(json) {
    def self = this
    json.with {
      servletContainerId self.getServletContainerId()
      servletContainerDescription self.getServletContainerDescription()
      if(sconfig.host)
        host sconfig.host
      if(sconfig.httpEnabled) {
        httpEnabled sconfig.httpEnabled
        if(sconfig.httpPort)
          httpPort sconfig.httpPort
        if(sconfig.httpIdleTimeout)
          httpIdleTimeout sconfig.httpIdleTimeout
      }
      if(sconfig.httpsEnabled) {
        httpsEnabled sconfig.httpsEnabled
        if(sconfig.httpsPort)
          httpsPort sconfig.httpsPort
        if(sconfig.httpsIdleTimeout)
          httpsIdleTimeout sconfig.httpsIdleTimeout
        if(sconfig.sslKeyStorePath)
          sslKeyStorePath self.fileToString(sconfig.sslKeyStorePath)
        if(sconfig.sslKeyStorePassword)
          sslKeyStorePassword sconfig.sslKeyStorePassword
        if(sconfig.sslKeyManagerPassword)
          sslKeyManagerPassword sconfig.sslKeyManagerPassword
        if(sconfig.sslTrustStorePath)
          sslTrustStorePath self.fileToString(sconfig.sslTrustStorePath)
        if(sconfig.sslTrustStorePassword)
          sslTrustStorePassword sconfig.sslTrustStorePassword
      }
      if(sconfig.realm)
        realm sconfig.realm
      if(sconfig.realmConfigFile)
        realmConfigFile self.fileToString(sconfig.realmConfigFile)
      if(sconfig.serverConfigFile)
        serverConfigFile self.fileToString(sconfig.serverConfigFile)
      writeLoggingConfig(json)
      if(config.baseDir)
        baseDir config.baseDir.absolutePath
      if(sconfig.singleSignOn != null)
        singleSignOn sconfig.singleSignOn
      if(sconfig.enableNaming != null)
        enableNaming sconfig.enableNaming
      if(config.productMode)
        productMode true
      webApps webAppConfigs.collect { WebAppConfig wconfig ->
        { ->
          inplace wconfig.inplace
          inplaceMode wconfig.inplaceMode
          self.writeWebAppClassPath(delegate, wconfig)
          contextPath wconfig.contextPath
          resourceBase self.fileToString(wconfig.resourceBase)
          if(wconfig.extraResourceBases)
            extraResourceBases wconfig.extraResourceBases.collect({ self.fileToString(it) })
          if(wconfig.initParameters)
            initParams wconfig.initParameters
          if(wconfig.realm)
            realm wconfig.realm
          if(wconfig.realmConfigFile)
            realmConfigFile self.fileToString(wconfig.realmConfigFile)
          if(wconfig.contextConfigFile)
            contextConfigFile self.fileToString(wconfig.contextConfigFile)
          if(wconfig.springBootSources)
            springBootSources wconfig.springBootSources
        }
      }
    }
  }

  protected void writeWebAppClassPath(json, WebAppConfig webAppConfig) {
    def classPathResolver = config.getWebAppClassPathResolver()
    if(classPathResolver) {
      def classPath = classPathResolver.resolveWebAppClassPath(webAppConfig)
      if(classPath)
        json.webappClassPath classPath
    }
  }
}

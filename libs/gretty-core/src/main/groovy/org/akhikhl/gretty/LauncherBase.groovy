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
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.Future

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
abstract class LauncherBase implements Launcher {

  protected static final Logger log = LoggerFactory.getLogger(LauncherBase)

  protected final LauncherConfig config
  protected final ServerConfig sconfig
  protected final Iterable<WebAppConfig> webAppConfigs

  protected ServiceProtocol.Reader reader
  protected ServiceProtocol.Writer writer
  protected Map serverStartInfo

  LauncherBase(LauncherConfig config) {
    this.config = config
    sconfig = config.getServerConfig()
    webAppConfigs = config.getWebAppConfigs()
  }

  protected void afterJavaExec() {
  }

  @Override
  void afterLaunch() {
    getPortPropertiesFile().delete()
  }

  protected void beforeJavaExec() {
  }

  @Override
  void beforeLaunch() {
    reader = ServiceProtocol.createReader()
    log.debug 'statusPort: {}', reader.port

    File portPropertiesFile = getPortPropertiesFile()
    portPropertiesFile.parentFile.mkdirs()
  }

  @Override
  void dispose() {
    reader?.with { IOUtils.closeQuietly(it) }
  }

  protected static fileToString(file) {
    file instanceof File ? file.absolutePath : file.toString()
  }

  protected abstract File getPortPropertiesFile()

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

  Map getServerStartInfo() {
    serverStartInfo
  }

  private void interactiveLoop() {
    def hint = 'Press \'q\' or \'Q\' to stop the server or any other key to restart.'
    System.out.println hint
    // Based on Jetty-Maven-Plugin console scanner
    infinite:
    while (true) {
      while (System.in.available() > 0) {
        def input = System.in.read()
        if (input >= 0) {
          char c = input as char
          if (c == 'q' || c == 'Q') {
            stopServer()
            break infinite
          } else {
            if (sconfig.interactiveMode == 'rebuildAndRestartOnKeyPress') {
              rebuildWebapps()
            }
            log.debug 'Sending command: {}', 'restartWithEvent'
            def futureResponse = reader.readMessageAsync()
            writer.write('restartWithEvent')
            // Waiting for restart complete event
            def status = futureResponse.get()
            log.debug "Received status: ${status}"
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
  }

  protected abstract void javaExec(JavaExecParams params)

  @Override
  void launch() {
    beforeLaunch()
    try {
      Thread thread = launchThread()
      if (config.getInteractive()) {
        if (sconfig.interactiveMode == 'restartOnKeyPress' || sconfig.interactiveMode == 'rebuildAndRestartOnKeyPress') {
          interactiveLoop()
        } else if (sconfig.interactiveMode == 'stopOnKeyPress') {
          System.out.println 'Press any key to stop the server.'
          System.in.read()
          stopServer()
        } else {
          log.warn 'Unexpected interactiveMode: {}', sconfig.interactiveMode
        }
      } else
        System.out.println "Run '${config.getStopCommand()}' to stop the server."
      thread.join()
    } finally {
      afterLaunch()
    }
  }

  @Override
  Thread launchThread() {

    for(WebAppConfig wconfig in webAppConfigs)
      prepareToRun(wconfig)

    Thread thread
    Future futureResponse = reader.readMessageAsync()

    thread = Thread.start {
      for(Closure c in sconfig.onStart) {
        c.delegate = sconfig
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c()
      }
      try {
        beforeJavaExec()
        try {
          JavaExecParams params = new JavaExecParams()
          params.main = 'org.akhikhl.gretty.Runner'
          params.args = ["--statusPort=${reader.port}", "--serverManagerFactory=${getServerManagerFactory()}"]
          params.debug = config.getDebug()
          params.debugSuspend = config.getDebugSuspend()
          params.debugPort = config.getDebugPort()
          params.jvmArgs = sconfig.jvmArgs
          params.systemProperties = sconfig.systemProperties
          if(!sconfig.secureRandom) {
            // Speeding up tomcat startup, according to https://wiki.apache.org/tomcat/HowTo/FasterStartUp
            // ATTENTION: replacing the blocking entropy source (/dev/random) with a non-blocking one
            // actually reduces security because you are getting less-random data.
            params.systemProperty 'java.security.egd', 'file:/dev/./urandom'
          }
          javaExec(params)
        } finally {
          afterJavaExec()
        }
      } finally {
        for(Closure c in sconfig.onStop) {
          c.delegate = sconfig
          c.resolveStrategy = Closure.DELEGATE_FIRST
          c()
        }
      }
    }

    def response = futureResponse.get()
    log.debug "Response {}", response
    if (response == 'started')
      throw new RuntimeException('Web-server is already running.')

    int servicePort = response.substring("init ".length()) as Integer
    writer = ServiceProtocol.createWriter(servicePort)

    Properties props = new Properties()
    props.servicePort = servicePort as String
    props.statusPort = reader.port as String
    portPropertiesFile.withWriter 'UTF-8', {
      props.store(it, null)
    }

    futureResponse = reader.readMessageAsync()
    def runConfigJson = getRunConfigJson()
    log.debug 'Sending parameters to port {}', writer.port
    log.debug runConfigJson.toPrettyString()
    writer.write(runConfigJson.toString())
    def status = futureResponse.get()
    log.debug 'Got start status: {}', status
    serverStartInfo = new JsonSlurper().parseText(status)

    if(serverStartInfo.error)
      throw new Exception(serverStartInfo.errorMessage)

    thread
  }

  protected void prepareToRun(WebAppConfig wconfig) {
    wconfig.prepareToRun()
  }

  protected abstract void rebuildWebapps()

  protected void stopServer() {
    log.debug 'Sending command: {}', 'stop'
    writer.write('stop')
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
        if(sconfig.sslNeedClientAuth)
          sslNeedClientAuth sconfig.sslNeedClientAuth
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
          if(wconfig.springBoot)
            springBoot true
          self.writeWebAppClassPath(delegate, wconfig)
          contextPath wconfig.contextPath
          webXml wconfig.webXml
          resourceBase self.fileToString(wconfig.resourceBase)
          if(wconfig.extraResourceBases)
            extraResourceBases wconfig.extraResourceBases.collect({ self.fileToString(it) })
          if(wconfig.initParameters)
            initParameters wconfig.initParameters
          if(wconfig.realm)
            realm wconfig.realm
          if(wconfig.realmConfigFile)
            realmConfigFile self.fileToString(wconfig.realmConfigFile)
          if(wconfig.contextConfigFile)
            contextConfigFile self.fileToString(wconfig.contextConfigFile)
          if(wconfig.springBootMainClass)
            springBootMainClass wconfig.springBootMainClass
          if(wconfig.webInfIncludeJarPattern)
            webInfIncludeJarPattern wconfig.webInfIncludeJarPattern

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

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

import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
abstract class LauncherBase implements Launcher {

  protected static int[] findFreePorts(int count, List<Integer> range = null) {
    List result = []
    try {
      List sockets = []
      try {
        if(!range) {
          while(count-- > 0) {
            ServerSocket socket = new ServerSocket(0)
            sockets.add(socket)
            result.add(socket.getLocalPort())
          }
        } else {
          for(Integer port in range) {
            try {
              ServerSocket socket = new ServerSocket(port)
              sockets.add(socket)
              result.add(socket.getLocalPort())
              if(--count == 0) {
                break;
              }
            } catch (IOException io) { }
          }
          if(count > 0) {
            throw new IllegalStateException("Unable to find enough ports");
          }
        }
      } finally {
        for(ServerSocket socket in sockets)
          socket.close()
      }
    } catch (IOException e) {
    }
    return result as int[]
  }

  protected static final Logger log = LoggerFactory.getLogger(LauncherBase)

  protected final LauncherConfig config
  protected final ServerConfig sconfig
  protected final Iterable<WebAppConfig> webAppConfigs
  protected ExecutorService executorService

  protected int servicePort = -1
  protected int statusPort = -1

  protected AsyncResponse asyncResponse

  protected Map serverStartInfo

  LauncherBase(LauncherConfig config) {
    this.config = config
    sconfig = config.getServerConfig()
    webAppConfigs = config.getWebAppConfigs()
    executorService = Executors.newSingleThreadExecutor()
  }

  protected void afterJavaExec() {
  }

  @Override
  void afterLaunch() {
    getPortPropertiesFile().delete()
    asyncResponse = null
  }

  protected void beforeJavaExec() {
  }

  @Override
  void beforeLaunch() {
    File portPropertiesFile = getPortPropertiesFile()
    portPropertiesFile.parentFile.mkdirs()
    if(portPropertiesFile.exists()) {
      Properties portProps = new Properties()
      portPropertiesFile.withReader 'UTF-8', {
        portProps.load(it)
      }
      int servicePort = portProps.servicePort as int
      int statusPort = portProps.statusPort as int
      def asyncResponse = new AsyncResponse(executorService, statusPort)
      if(asyncResponse.getStatus(servicePort) == 'started')
        throw new RuntimeException('Web-server is already running.')
    }
    (servicePort, statusPort) = findFreePorts(2, sconfig.auxPortRange)
    log.debug 'servicePort: {}, statusPort: {}', servicePort, statusPort
    Properties props = new Properties()
    props.servicePort = servicePort as String
    props.statusPort = statusPort as String
    portPropertiesFile.withWriter 'UTF-8', {
      props.store(it, null)
    }
    asyncResponse = new AsyncResponse(executorService, statusPort)
  }

  @Override
  void dispose() {
    executorService.shutdown()
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
            def futureResponse = asyncResponse.getResponse()
            ServiceProtocol.send(servicePort, 'restartWithEvent')
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

    def status = asyncResponse.getStatus(servicePort)

    if(status == 'started')
      throw new RuntimeException('Web-server is already running.')

    Future futureResponse = asyncResponse.getResponse()

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
          params.args = [ "--servicePort=${servicePort}", "--statusPort=${statusPort}", "--serverManagerFactory=${getServerManagerFactory()}" ]
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

    futureResponse.get()

    futureResponse = asyncResponse.getResponse()
    def runConfigJson = getRunConfigJson()
    log.debug 'Sending parameters to port {}', servicePort
    log.debug runConfigJson.toPrettyString()
    ServiceProtocol.send(servicePort, runConfigJson.toString())
    status = futureResponse.get()
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
    ServiceProtocol.send(servicePort, 'stop')
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
            initParams wconfig.initParameters
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

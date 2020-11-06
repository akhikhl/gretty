/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import groovy.cli.commons.CliBuilder
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
final class Runner {

  protected final Map params

  static class ServerStartEventImpl implements ServerStartEvent {
    private final ServiceProtocol.Writer writer

    ServerStartEventImpl(final ServiceProtocol.Writer writer) {
      this.writer = writer
    }

    @Override
    void onServerStart(Map serverStartInfo) {
      JsonBuilder json = new JsonBuilder()
      json serverStartInfo
      writer.writeMayFail(json.toString())
    }
  }

  static void main(String[] args) {
    def cli = new CliBuilder()
    cli.with {
      d longOpt: 'debug', type: Boolean, 'enable debug logging'
      st longOpt: 'statusPort', required: true, args: 1, argName: 'statusPort', type: Integer, 'status port'
      smf longOpt: 'serverManagerFactory', required: true, args: 1, argName: 'serverManagerFactory', type: String, 'server manager factory'
    }
    def options = cli.parse(args)
    Map params = [statusPort: options.statusPort as int, serverManagerFactory: options.serverManagerFactory, debug: options.debug]
    new Runner(params).run()
  }

  void initLogback(Map serverParams) {
    LoggerContext logCtx = LoggerFactory.getILoggerFactory()
    logCtx.stop()
    Map loggerCache = LoggerFactory.getILoggerFactory().@loggerCache
    for(String loggerName in new HashSet(loggerCache.keySet()))
      if(!loggerName.startsWith('org.eclipse.jetty'))
        loggerCache.remove(loggerName)
    String logbackConfigText
    if(serverParams.logbackConfigFile) {
      if(serverParams.logbackConfigFile.endsWith('.xml')) {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(logCtx)
        configurator.doConfigure(new File(serverParams.logbackConfigFile))
        return
      }
      logbackConfigText = new File(serverParams.logbackConfigFile).getText('UTF-8')
    }
    else
      logbackConfigText = Runner.class.getResourceAsStream('/grettyRunnerLogback.groovy').getText('UTF-8')
    Binding binding = new Binding()
    binding.loggingLevel = stringToLoggingLevel(serverParams.loggingLevel)
    binding.consoleLogEnabled = Boolean.valueOf(serverParams.consoleLogEnabled == null ? true : serverParams.consoleLogEnabled)
    binding.fileLogEnabled = Boolean.valueOf(serverParams.fileLogEnabled == null ? true : serverParams.fileLogEnabled)
    binding.logFileName = serverParams.logFileName
    binding.logDir = serverParams.logDir
    binding.grettyDebug = params.debug
    // FIXME #162 logback has temporarily suspended GafferConfigurator.
    // We do not know if and when it will come back, see news of 2019-10-11:
    // http://logback.qos.ch/news.html
    // new GafferConfiguratorEx(logCtx).run(binding, logbackConfigText)
    // Instead we use a fixed configuration file, which does not respect all the different
    // logging toggles in Gretty:
    Runner.class.getResource('/grettyRunnerLogback.xml').withInputStream {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(logCtx)
      configurator.doConfigure(it)
    }
  }

  private static Level stringToLoggingLevel(String str) {
    switch(str?.toUpperCase()) {
      case 'ALL':
        return Level.ALL
      case 'DEBUG':
        return Level.DEBUG
      case 'ERROR':
        return Level.ERROR
      case 'INFO':
        return Level.INFO
      case 'OFF':
        return Level.OFF
      case 'TRACE':
        return Level.TRACE
      case 'WARN':
        return Level.WARN
      default:
        return Level.INFO
    }
  }

  private Runner(Map params) {
    this.params = params
  }

  private void run() {
    LogUtil.setLevel(params.debug)
    boolean paramsLoaded = false
    def ServerManagerFactory = Class.forName(params.serverManagerFactory, true, this.getClass().classLoader)
    ServerManager serverManager = ServerManagerFactory.createServerManager()

    final def reader = ServiceProtocol.createReader()
    final def writer = ServiceProtocol.createWriter(params.statusPort)
    try {
      writer.write("init ${reader.port}")
      while(true) {
        def data = reader.readMessage()
        if(!paramsLoaded) {
          params << new JsonSlurper().parseText(data)
          paramsLoaded = true
          if(!Boolean.valueOf(System.getProperty('grettyProduct')))
            initLogback(params)
          serverManager.setParams(params)
          serverManager.startServer(new ServerStartEventImpl(writer))
          // Note that server is already in listening state.
          // If client sends a command immediately after 'started' signal,
          // the command is queued, so that socket.accept gets it anyway.
          continue
        }
        if(data == 'status')
          writer.write('started')
        else if(data == 'stop') {
          serverManager.stopServer()
          break
        }
        else if(data == 'restart') {
          serverManager.stopServer()
          serverManager.startServer(null)
        }
        else if(data == 'restartWithEvent') {
          serverManager.stopServer()
          serverManager.startServer(new ServerStartEventImpl(writer))
        }
        else if (data.startsWith('redeploy ')) {
          List<String> webappList = data.replace('redeploy ', '').split(' ').toList()
          serverManager.redeploy(webappList)
          writer.writeMayFail('redeployed')
        }
      }
    } finally {
      reader.close()
    }
  }
}

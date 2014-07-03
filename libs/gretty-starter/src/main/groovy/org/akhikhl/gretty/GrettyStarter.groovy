/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonSlurper

/**
 *
 * @author akhikhl
 */
class GrettyStarter {

  static void main(String[] args) {

    def cli = new CliBuilder()
    cli.with {
      r longOpt: 'run', 'run'
      s longOpt: 'start', 'start'
      st longOpt: 'stop', 'stop'
      re longOpt: 'restart', 'restart'
      d longOpt: 'basedir', args: 1, argName: 'basedir', type: String, required: true, 'basedir'
    }
    def options = cli.parse(args)
    File basedir = new File(options.basedir)
    String command
    if(options.start)
      command = 'start'
    else if(options.stop)
      command = 'stop'
    else if(options.restart)
      command = 'restart'
    else
      command = 'run'

    Map config
    new File(basedir, 'conf/server.json').withReader {
      config = new JsonSlurper().parse(it)
    }

    ServerConfig sconfig = new ServerConfig()
    config.serverConfig.each { key, value ->
      sconfig[key] = value
    }

    ConfigUtils.complementProperties(sconfig, ServerConfig.getDefaultServerConfig(config.productName))

    if(sconfig.jettyXmlFile) {
      File jettyXmlFile = new File(sconfig.jettyXmlFile)
      if(!jettyXmlFile.isAbsolute())
        jettyXmlFile = new File(basedir, sconfig.jettyXmlFile)
      sconfig.jettyXmlFile = jettyXmlFile.exists() ? jettyXmlFile : null
    }

    if(sconfig.logbackConfigFile) {
      File logbackConfigFile = new File(sconfig.logbackConfigFile)
      if(!logbackConfigFile.isAbsolute())
        logbackConfigFile = new File(basedir, sconfig.logbackConfigFile)
      sconfig.logbackConfigFile = logbackConfigFile.exists() ? logbackConfigFile : null
    }

    if(command == 'stop' || command == 'restart') {
      ServiceProtocol.send(sconfig.servicePort, command)
      return
    }

    List<WebAppConfig> wconfigs = []
    config.webApps.each { w ->
      WebAppConfig wconfig = new WebAppConfig()
      w.each { key, value ->
        wconfig[key] = value
      }
      if(wconfig.warResourceBase) {
        File warFile = new File(wconfig.warResourceBase)
        if(!warFile.isAbsolute())
          warFile = new File(basedir, wconfig.warResourceBase)
        wconfig.warResourceBase = warFile.absolutePath
      }
      wconfigs.add(wconfig)
    }

    def launcherConfig = new LauncherConfig() {

      boolean getDebug() {
        false
      }

      boolean getInteractive() {
        command == 'run'
      }

      boolean getManagedClassReload() {
        false
      }

      ServerConfig getServerConfig() {
        sconfig
      }

      String getStopCommand() {
        PlatformUtils.isWindows() ? 'stop.bat' : './stop.sh'
      }

      WebAppClassPathResolver getWebAppClassPathResolver() {
      }

      Iterable<WebAppConfig> getWebAppConfigs() {
        wconfigs
      }
    }

    new StarterLauncher(basedir, config.servetContainer, launcherConfig).launch()
  }
}


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

    File basedir = new File(GrettyStarter.class.getProtectionDomain().getCodeSource().getLocation().getPath().toURI().getPath()).parentFile.parentFile

    def cli = new CliBuilder()
    // here we could define command-line parameters via CliBuilder DSL

    def options = cli.parse(args)

    String command = 'run'

    def cliArgs = options.arguments()
    if(cliArgs)
      command = cliArgs[0]

    Map config
    new File(basedir, 'conf/server.json').withReader {
      config = new JsonSlurper().parse(it)
    }

    ServerConfig sconfig = new ServerConfig()
    config.serverConfig.each { key, value ->
      sconfig[key] = value
    }

    ConfigUtils.complementProperties(sconfig, ServerConfig.getDefaultServerConfig(config.productName))

    def resolveFile = { f ->
      if(f) {
        File file = new File(f)
        if(!file.isAbsolute())
          file = new File(basedir, f)
        file.exists() ? file : null
      }
    }

    sconfig.sslKeyStorePath = resolveFile(sconfig.sslKeyStorePath)
    sconfig.sslTrustStorePath = resolveFile(sconfig.sslTrustStorePath)
    sconfig.realmConfigFile = resolveFile(sconfig.realmConfigFile)
    sconfig.serverConfigFile = resolveFile(sconfig.serverConfigFile)
    sconfig.logbackConfigFile = resolveFile(sconfig.logbackConfigFile)

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
      wconfig.resourceBase = resolveFile(wconfig.resourceBase)
      wconfig.realmConfigFile = resolveFile(wconfig.realmConfigFile)
      wconfig.contextConfigFile = resolveFile(wconfig.contextConfigFile)
      if(wconfig.extraResourceBases)
        wconfig.extraResourceBases = wconfig.extraResourceBases.collect { resolveFile(it) }
      wconfigs.add(wconfig)
    }

    def launcherConfig = new LauncherConfig() {

      boolean getDebug() {
        false
      }

      boolean getInteractive() {
        command != 'start'
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

      File getBaseDir() {
        basedir
      }

      WebAppClassPathResolver getWebAppClassPathResolver() {
      }

      Iterable<WebAppConfig> getWebAppConfigs() {
        wconfigs
      }
    }

    new StarterLauncher(basedir, config, launcherConfig).launch()
  }
}

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
      st longOpt: 'restart', 'restart'
      d longOpt: 'basedir', args: 1, argName: 'basedir', type: String, 'basedir'
    }
    def options = cli.parse(args)
    String basedir = options.basedir
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
        System.getProperty('os.name', 'generic').toLowerCase().indexOf('win') >= 0 ? 'stop.bat' : 'stop.sh'
      }

      WebAppClassPathResolver getWebAppClassPathResolver() {

      }

      Iterable<WebAppConfig> getWebAppConfigs() {
        wconfigs
      }
    }

    new StarterLauncher(launcherConfig).launch()
  }
}


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
    }
    def options = cli.parse(args)
    String command
    if(options.start)
      command = 'start'
    else if(options.stop)
      command = 'stop'
    else if(options.restart)
      command = 'restart'
    else
      command = 'run'

    Map starterConfig
    GrettyStarter.getResourceAsStream('/gretty-starter/gretty-starter-config.json').withReader {
      starterConfig = new JsonSlurper().parse(it)
    }
    
    ServerConfig sconfig = new ServerConfig()
    starterConfig.sconfig.each { key, value ->
      sconfig[key] = value
    }
    
    if(command == 'stop' || command == 'restart') {
      ServiceProtocol.send(sconfig.servicePort, command)
      return
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
        starterConfig.stopCommand
      }

      WebAppClassPathResolver getWebAppClassPathResolver() {

      }

      Iterable<WebAppConfig> getWebAppConfigs() {

      }
    }

    new StarterLauncher(launcherConfig).launch()
  }
}


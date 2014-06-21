/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Gradle task for control over jetty
 *
 * @author akhikhl
 */
abstract class AppServiceTask extends DefaultTask {

  private static Logger log = LoggerFactory.getLogger(AppServiceTask)

  Integer servicePort

  @TaskAction
  void action() {
    ServerConfig serverConfig = new ServerConfig(servicePort: servicePort)
    ConfigUtils.complementProperties(serverConfig, project.gretty.serverConfig, ServerConfig.getDefault(project))
    serverConfig.resolve(project)
    log.debug 'Sending command {} to port {}', command, serverConfig.servicePort
    ServiceProtocol.send(serverConfig.servicePort, command)
  }

  abstract String getCommand()
}

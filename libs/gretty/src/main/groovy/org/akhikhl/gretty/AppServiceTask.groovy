/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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
    ConfigUtils.complementProperties(serverConfig, project.gretty.serverConfig, ProjectUtils.getDefaultServerConfig(project))
    ProjectUtils.resolveServerConfig(project, serverConfig)
    log.debug 'Sending command {} to port {}', command, serverConfig.servicePort
    ServiceProtocol.send(serverConfig.servicePort, command)
  }

  abstract String getCommand()
}

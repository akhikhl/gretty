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
import org.gradle.api.GradleException
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

  /**
   * Please don't use servicePort, it will be removed in Gretty 2.0
   */
  @Deprecated
  Integer servicePort

  @TaskAction
  void action() {
    String command = getCommand()
    File portPropertiesFile = DefaultLauncher.getPortPropertiesFile(project)
    if(!portPropertiesFile.exists())
      throw new GradleException("Gretty seems to be not running, cannot send command '$command' to it.")
    Properties portProps = new Properties()
    portPropertiesFile.withReader 'UTF-8', {
      portProps.load(it)
    }
    int servicePort = portProps.servicePort as int
    ServerConfig serverConfig = new ServerConfig(servicePort: servicePort)
    ConfigUtils.complementProperties(serverConfig, project.gretty.serverConfig, ProjectUtils.getDefaultServerConfig(project))
    ProjectUtils.resolveServerConfig(project, serverConfig)
    log.debug 'Sending command {} to port {}', command, servicePort
    ServiceProtocol.send(servicePort, command)
  }

  abstract String getCommand()
}

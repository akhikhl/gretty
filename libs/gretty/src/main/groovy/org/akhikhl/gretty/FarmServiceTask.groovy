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
 *
 * @author akhikhl
 */
abstract class FarmServiceTask extends DefaultTask {

  private static Logger log = LoggerFactory.getLogger(FarmServiceTask)

  String farmName = ''

  /**
   * Please don't use servicePort, it will be removed in Gretty 2.0
   */
  @Deprecated
  Integer servicePort

  @TaskAction
  void action() {
    String command = getCommand()
    //
    FarmConfigurer configurer = new FarmConfigurer(project)
    FarmExtension farm = new FarmExtension(project)
    configurer.configureFarm(farm, configurer.getProjectFarm(farmName))
    //
    File portPropertiesFile = DefaultLauncher.getPortPropertiesFile(project, farm.serverConfig)
    if(!portPropertiesFile.exists())
      throw new GradleException("Gretty seems to be not running, cannot send command '$command' to it.")
    Properties portProps = new Properties()
    portPropertiesFile.withReader 'UTF-8', {
      portProps.load(it)
    }
    int servicePort = portProps.servicePort as int
    //
    log.debug 'Sending command {} to port {}', command, servicePort
    ServiceProtocol.send(servicePort, command)
  }

  abstract String getCommand()
}

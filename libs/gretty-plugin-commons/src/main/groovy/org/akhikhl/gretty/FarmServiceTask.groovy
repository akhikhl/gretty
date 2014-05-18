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
 *
 * @author akhikhl
 */
abstract class FarmServiceTask extends DefaultTask {

  private static Logger log = LoggerFactory.getLogger(FarmServiceTask)

  String farmName = ''

  Integer servicePort

  @TaskAction
  void action() {
    FarmConfigurer configurer = new FarmConfigurer(project)
    Farm farm = new Farm(servicePort: servicePort)
    configurer.configureFarm(farm, configurer.getProjectFarm(farmName))
    log.debug 'Sending command {} to port {}', command, farm.servicePort
    ServiceControl.send(farm.servicePort, command)
  }

  abstract String getCommand()
}

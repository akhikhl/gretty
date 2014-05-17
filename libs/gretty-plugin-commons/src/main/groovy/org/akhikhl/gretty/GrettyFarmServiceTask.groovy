/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.GradleException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class GrettyFarmServiceTask extends GrettyBaseTask {

  private static Logger log = LoggerFactory.getLogger(GrettyFarmServiceTask)

  String farmName = ''

  Integer servicePort
  String command

  @Override
  void action() {
    log.debug 'Sending command {} to port {}', command, servicePort
    ServiceControl.send(servicePort, command)
  }

  @Override
  protected void resolveProperties() {
    requireProperty 'command'
    if(servicePort == null) {
      Farm farm = new Farm()
      def sourceFarm = project.farms.farmsMap[farmName]
      if(!sourceFarm)
        throw new GradleException("Farm '${farmName}' referenced in GrettyFarmServiceTask is not defined in project farms")
      ConfigUtils.complementProperties(farm.serverConfig, sourceFarm.serverConfig, ServerConfig.getDefault(project))
      farm.serverConfig.resolve(project)
      servicePort = farm.servicePort
    }
  }
}

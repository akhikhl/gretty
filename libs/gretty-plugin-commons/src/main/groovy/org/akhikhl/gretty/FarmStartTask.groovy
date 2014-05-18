/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class FarmStartTask extends StartBaseTask {

  String farmName = ''

  @Delegate
  protected Farm farm = new Farm()

  boolean inplace = true

  @Override
  protected RunConfig getRunConfig() {

    FarmConfigurer configurer = new FarmConfigurer(project)

    Farm tempFarm = new Farm()
    configurer.configureFarm(tempFarm, farm, configurer.getProjectFarm(farmName))

    List<WebAppConfig> wconfigs = []
    configurer.resolveWebAppRefs(tempFarm.webAppRefs, wconfigs, inplace)

    new RunConfig() {

      ServerConfig getServerConfig() {
        tempFarm.serverConfig
      }

      Iterable<WebAppConfig> getWebAppConfigs() {
        wconfigs
      }
    }
  }

  Iterable<WebAppConfig> getWebAppConfigsForProjects() {
    FarmConfigurer configurer = new FarmConfigurer(project)
    Map wrefs = [:]
    FarmConfigurer.mergeWebAppRefMaps(wrefs, farm.webAppRefs)
    FarmConfigurer.mergeWebAppRefMaps(wrefs, configurer.getProjectFarm(farmName).webAppRefs)
    if(!wrefs)
      wrefs = configurer.getDefaultWebAppRefMap()
    configurer.getWebAppConfigsForProjects(wrefs, inplace)
  }
}

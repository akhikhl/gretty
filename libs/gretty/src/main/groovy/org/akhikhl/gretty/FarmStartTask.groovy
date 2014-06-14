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
  protected ServerConfig serverConfig = new ServerConfig()

  // key is project path or war path, value is options
  protected Map webAppRefs = [:]

  boolean inplace = true

  @Override
  RunConfig getRunConfig() {

    FarmConfigurer configurer = new FarmConfigurer(project)

    Farm tempFarm = new Farm()
    configurer.configureFarm(tempFarm, new Farm(serverConfig: serverConfig, webAppRefs: webAppRefs), configurer.getProjectFarm(farmName))

    List<WebAppConfig> wconfigs = []
    configurer.resolveWebAppRefs(tempFarm.webAppRefs, wconfigs, inplace)

    new RunConfig() {

      String getJettyVersion() {
        JettyVersionResolver.resolve(tempFarm.jettyVersion)
      }

      boolean getManagedClassReload() {
        tempFarm.managedClassReload
      }

      ServerConfig getServerConfig() {
        tempFarm.serverConfig
      }

      Iterable<WebAppConfig> getWebAppConfigs() {
        wconfigs
      }
    }
  }

  @Override
  protected String getStopTaskName() {
    'farmStop' + farmName
  }

  Iterable<WebAppConfig> getWebAppConfigsForProjects() {
    FarmConfigurer configurer = new FarmConfigurer(project)
    Map wrefs = [:]
    FarmConfigurer.mergeWebAppRefMaps(wrefs, webAppRefs)
    FarmConfigurer.mergeWebAppRefMaps(wrefs, configurer.getProjectFarm(farmName).webAppRefs)
    if(!wrefs)
      wrefs = configurer.getDefaultWebAppRefMap()
    configurer.getWebAppConfigsForProjects(wrefs, inplace)
  }

  Iterable<Project> getWebAppProjects() {
    FarmConfigurer configurer = new FarmConfigurer(project)
    Map wrefs = [:]
    FarmConfigurer.mergeWebAppRefMaps(wrefs, webAppRefs)
    FarmConfigurer.mergeWebAppRefMaps(wrefs, configurer.getProjectFarm(farmName).webAppRefs)
    if(!wrefs)
      wrefs = configurer.getDefaultWebAppRefMap()
    configurer.getWebAppProjects(wrefs)
  }

  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webAppRefs[w] = options
  }
}

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
  protected StartConfig getStartConfig() {

    FarmConfigurer configurer = new FarmConfigurer(project)

    FarmExtension tempFarm = new FarmExtension()
    configurer.configureFarm(tempFarm, new FarmExtension(serverConfig: serverConfig, webAppRefs: webAppRefs), configurer.getProjectFarm(farmName), new FarmExtension(servletContainer: 'jetty9', managedClassReload: true))
    doPrepareServerConfig(tempFarm.serverConfig)

    List<WebAppConfig> wconfigs = []
    configurer.resolveWebAppRefs(tempFarm.webAppRefs, wconfigs, inplace)
    for(WebAppConfig wconfig in wconfigs)
      doPrepareWebAppConfig(wconfig)

    new StartConfig() {

      @Override
      ServerConfig getServerConfig() {
        tempFarm.serverConfig
      }

      @Override
      Iterable<WebAppConfig> getWebAppConfigs() {
        wconfigs
      }
    }
  }

  @Override
  protected String getStopCommand() {
    "gradle farmStop${farmName}"
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

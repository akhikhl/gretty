/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class FarmStartTask extends StartBaseTask implements ServerConfigWithInputs {

  String farmName = ''

  @Delegate
  protected final ServerConfig serverConfig = new ServerConfig()

  // key is project path or war path, value is options
  protected final Map webAppRefs = [:]

  // list of projects or project paths
  protected final List integrationTestProjects = []

  boolean inplace = true

  // specifying default inplaceMode to override (optionally) by child applications
  String inplaceMode = 'soft'

  Iterable<Project> getIntegrationTestProjects() {
    FarmConfigurer configurer = new FarmConfigurer(project)
    Set<Project> result = new LinkedHashSet()
    result.addAll(getWebAppProjects())
    result.addAll(configurer.getIntegrationTestProjects(this.integrationTestProjects + configurer.getProjectFarm(farmName).integrationTestProjects))
    result
  }

  @Override
  protected StartConfig getStartConfig() {

    FarmConfigurer configurer = new FarmConfigurer(project)

    FarmExtension tempFarm = new FarmExtension(project)
    configurer.configureFarm(tempFarm, new FarmExtension(project, serverConfig: serverConfig, webAppRefs: webAppRefs), configurer.getProjectFarm(farmName), new FarmExtension(project))
    doPrepareServerConfig(tempFarm.serverConfig)

    List<WebAppConfig> wconfigs = []
    configurer.resolveWebAppRefs(farmName, tempFarm.webAppRefs, wconfigs, inplace, inplaceMode)
    for(WebAppConfig wconfig in wconfigs) {
      doPrepareWebAppConfig(wconfig)

      if(wconfig.inplace && wconfig.inplaceMode == 'hard') {
          logger.warn("You\'re running webapp (${wconfig.projectPath}) in hard inplaceMode: Overlay and filtering features of gretty will be disabled!")
      }

      if(wconfig.webXml) {
        logger.warn('You\'ve configured gretty to use the web.xml file located at ' + wconfig.webXml + '.')
      }
    }

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
    if(!wrefs && !configurer.getProjectFarm(farmName).includes)
      wrefs = configurer.getDefaultWebAppRefMap()
    configurer.getWebAppConfigsForProjects(wrefs, inplace, inplaceMode)
  }

  Iterable<Project> getWebAppProjects() {
    FarmConfigurer configurer = new FarmConfigurer(project)
    Map wrefs = [:]
    FarmConfigurer.mergeWebAppRefMaps(wrefs, webAppRefs)
    FarmConfigurer.mergeWebAppRefMaps(wrefs, configurer.getProjectFarm(farmName).webAppRefs)
    if(!wrefs && !configurer.getProjectFarm(farmName).includes)
      wrefs = configurer.getDefaultWebAppRefMap()
    configurer.getWebAppProjects(wrefs)
  }

  void integrationTestProject(Object project) {
    if(project instanceof Project)
      project = project.path
    integrationTestProjects.add(project)
  }

  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webAppRefs[w] = options
  }

  // use serverConfigFile instead
  @Deprecated
  def getJettyXmlFile() {
    serverConfig.getJettyXmlFile()
  }

  // use serverConfigFile instead
  @Deprecated
  void setJettyXmlFile(newValue) {
    serverConfig.setJettyXmlFile(newValue)
  }
}

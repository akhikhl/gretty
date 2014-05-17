/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class GrettyFarmStartTask extends GrettyStartBaseTask {

  String farmName = ''

  @Delegate
  protected Farm farm = new Farm()

  protected List<WebAppConfig> webAppConfigs = []

  protected boolean inplace = true

  @Override
  protected ServerConfig getServerConfig() {
    farm.serverConfig
  }

  @Override
  protected List<WebAppConfig> getWebApps() {
    webAppConfigs
  }

  @Override
  protected void resolveProperties() {
    def sourceFarm = project.farms.farmsMap[farmName]
    if(!sourceFarm)
      throw new GradleException("Farm '${farmName}' referenced in GrettyFarmStartTask is not defined in project farms")
    ConfigUtils.complementProperties(farm.serverConfig, sourceFarm.serverConfig, ServerConfig.getDefault(project))
    farm.serverConfig.resolve(project)
    sourceFarm.webapps.each { w, options ->
      def existingOptions = farm.webapps[w]
      if(existingOptions == null)
        existingOptions = farm.webapps[w] = [:]
      existingOptions << options
    }
    if(!farm.webapps)
      farm.webapps = project.subprojects.findAll { it.extensions.findByName('gretty') }.inject [:], { map, p ->
        map[p.path] = [:]
        map
      }
    farm.webapps.each { w, options ->
      def proj = project.findProject(w)
      if(!proj)
        throw new GradleException("Could not resolve project '${w}' referenced in ${farmName ? 'farm ' + farmName : 'default farm'}")
      if(!proj.extensions.findByName('gretty'))
        throw new GradleException("${proj} does not contain gretty extension. Please make sure that gretty plugin is applied to it.")
      if(proj.ext.grettyPluginJettyVersion != project.ext.grettyFarmPluginJettyVersion)
        throw new GradleException("${proj} uses jetty version ${proj.ext.grettyPluginJettyVersion} different from version ${project.ext.grettyFarmPluginJettyVersion} used by farm.")
      WebAppConfig webappConfig = new WebAppConfig()
      ConfigUtils.complementProperties(webappConfig, options, proj.gretty.webAppConfig, WebAppConfig.getDefault(proj))
      // individual webapp may override task-wide inplace
      if(webappConfig.inplace == null) webappConfig.inplace = inplace
      webappConfig.resolve(proj)
      webAppConfigs.add(webappConfig)
    }
    super.resolveProperties()
  }
}

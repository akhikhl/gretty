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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class GrettyFarmStartTask extends GrettyStartBaseTask {

  private static final Logger log = LoggerFactory.getLogger(GrettyFarmStartTask)

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
    String farmDescr = farmName ? 'farm ' + farmName : 'default farm'
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
      WebAppConfig webappConfig = new WebAppConfig()
      def proj = project.findProject(w)
      if(proj == null) {
        log.warn '{}: {} is not an existing project, treating it as a maven dependency', farmDescr, w
        def gav = w.split(':')
        proj = project.rootProject.allprojects.find {
          it.group == gav[0] && it.name == gav[1]
        }
        if(proj)
          log.warn '{}: {} actually comes from project {}, so using project instead', farmDescr, w, proj.path
      }
      if(proj) {
        if(!proj.extensions.findByName('gretty'))
          throw new GradleException("${proj} does not contain gretty extension. Please make sure that gretty plugin is applied to it.")
        if(proj.ext.grettyPluginJettyVersion != project.ext.grettyFarmPluginJettyVersion)
          throw new GradleException("${proj} uses jetty version ${proj.ext.grettyPluginJettyVersion} different from version ${project.ext.grettyFarmPluginJettyVersion} used by farm.")
        ConfigUtils.complementProperties(webappConfig, options, proj.gretty.webAppConfig, WebAppConfig.getDefault(proj))
        // individual webapp may override task-wide inplace
        if(webappConfig.inplace == null) webappConfig.inplace = inplace
      } else {
        project.configurations.maybeCreate('farm')
        project.dependencies.add 'farm', w
        ConfigUtils.complementProperties(webappConfig, options, WebAppConfig.getDefaultForDependency(project, w))
        // maven dependency is always war-based
        webappConfig.inplace = false
      }
      webappConfig.resolve(proj)
      webAppConfigs.add(webappConfig)
    }
    super.resolveProperties()
  }
}

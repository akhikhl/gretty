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
class GrettyStartFarmTask extends GrettyStartBaseTask {

  String farmName = ''

  @Delegate
  protected Farm farm = new Farm()

  private List<WebAppConfig> webAppConfigs = []

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
      throw new GradleException("Farm '${farmName}' referenced in GrettyStartFarmTask is not defined in project farms")
    ConfigUtils.complementProperties(farm.serverConfig, sourceFarm.serverConfig, ServerConfig.getDefault(project))
    farm.serverConfig.resolve(project)
    farm.webapps += sourceFarm.webapps
    if(!farm.webapps)
      farm.webapps = project.subprojects.findAll { it.extensions.findByName('gretty') }.collect { it.path }
    for(def w in farm.webapps) {
      def proj = (w instanceof Project ? w : project.project(w))
      if(!proj)
        throw new GradleException("Could not resolve project '${w}' referenced in ${farmName ? 'farm ' + farmName : 'default farm'}")
      if(!proj.extensions.findByName('gretty'))
        throw new GradleException("${proj} does not contain gretty extension. Please make sure that gretty plugin is applied to it.")
      WebAppConfig webapp = new WebAppConfig()
      ConfigUtils.complementProperties(webapp, proj.gretty.webAppConfig, WebAppConfig.getDefault(proj))
      webapp.resolve(proj)
      webAppConfigs.add(webapp)
    }
    super.resolveProperties()
  }
}

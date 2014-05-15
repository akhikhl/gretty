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

  @Override
  protected ServerConfig getServerConfig() {
    farm.serverConfig
  }

  @Override
  protected List<WebAppRunConfig> getWebApps() {
    farm.webapps.collect { w ->
      def proj = (w instanceof Project ? w : project.project(w))
      if(!proj)
        throw new GradleException("Could not resolve project ${w} referenced in farm definition")
      if(!proj.extensions.findByName('gretty'))
        throw new GradleException("${proj} does not contain gretty extension. Please make sure that 'gretty-plugin' is applied to it.")
      proj.gretty.webAppConfig
    }
  }

  @Override
  protected void setupProperties() {
    serverConfig.setupProperties(project, project.gretty.serverConfig)
    webAppConfig.setupProperties(project, project.gretty.webAppConfig)
    super.setupProperties()
  }
}

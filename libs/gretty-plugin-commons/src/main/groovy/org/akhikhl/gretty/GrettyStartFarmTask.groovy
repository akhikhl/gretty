/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.GradleException

/**
 *
 * @author akhikhl
 */
class GrettyStartFarmTask extends GrettyStartBaseTask {

  String farmName = ''

  @Delegate
  protected Farm farm = new Farm()

  @Override
  protected List<WebAppRunConfig> getWebApps() {
  }

  @Override
  protected void setupProperties() {
    serverConfig.setupProperties([project], project.gretty.serverConfig)
    webAppConfig.setupProperties(project, project.gretty.webAppConfig)
    super.setupProperties()
  }
}

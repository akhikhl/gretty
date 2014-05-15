/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 * Gradle task for starting jetty
 *
 * @author akhikhl
 */
class GrettyStartTask extends GrettyStartBaseTask {

  @Delegate
  private ServerRunConfig serverConfig = new ServerRunConfig()

  @Delegate
  private WebAppRunConfig webAppConfig = new WebAppRunConfig()

  @Override
  protected List<WebAppRunConfig> getWebApps() {
    [ webAppConfig ]
  }

  @Override
  protected void setupProperties() {
    serverConfig.setupProperties(project, project.gretty.serverConfig)
    webAppConfig.setupProperties(project, project.gretty.webAppConfig)
    super.setupProperties()
  }
}

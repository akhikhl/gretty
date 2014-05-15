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
  private ServerConfig serverConfig = new ServerConfig()

  @Delegate
  private WebAppConfig webAppConfig = new WebAppConfig()

  @Override
  protected ServerConfig getServerConfig() {
    serverConfig
  }

  @Override
  protected List<WebAppConfig> getWebApps() {
    [ webAppConfig ]
  }

  @Override
  protected void setupProperties() {
    ConfigUtils.complementProperties(serverConfig, project.gretty.serverConfig, ServerConfig.getDefault(project))
    serverConfig.resolve(project)
    ConfigUtils.complementProperties(webAppConfig, project.gretty.webAppConfig, WebAppConfig.getDefault(project))
    webAppConfig.resolve(project)
    super.setupProperties()
  }
}

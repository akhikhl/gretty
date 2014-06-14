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
class JettyStartTask extends StartBaseTask {

  @Delegate
  private ServerConfig serverConfig = new ServerConfig()

  @Delegate
  private WebAppConfig webAppConfig = new WebAppConfig()

  protected boolean getEffectiveInplace() {
    if(webAppConfig.inplace != null)
      webAppConfig.inplace
    else if(project.gretty.webAppConfig.inplace != null)
      project.gretty.webAppConfig.inplace
    else
      true
  }

  @Override
  RunConfig getRunConfig() {

    ServerConfig sconfig = new ServerConfig()
    ConfigUtils.complementProperties(sconfig, serverConfig, project.gretty.serverConfig, ServerConfig.getDefault(project))
    sconfig.resolve(project)

    WebAppConfig wconfig = new WebAppConfig()
    ConfigUtils.complementProperties(wconfig, webAppConfig, project.gretty.webAppConfig, WebAppConfig.getDefaultForProject(project), new WebAppConfig(inplace: true))
    wconfig.resolve(project)

    new RunConfig() {

      String getServletContainer() {
        project.gretty.servletContainer
      }

      boolean getManagedClassReload() {
        project.gretty.managedClassReload
      }

      ServerConfig getServerConfig() {
        sconfig
      }

      Iterable<WebAppConfig> getWebAppConfigs() {
        [ wconfig ]
      }
    }
  }

  @Override
  protected String getStopTaskName() {
    'jettyStop'
  }
}

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
  LauncherConfig getLauncherConfig() {
  
    def self = this

    ServerConfig sconfig = new ServerConfig()
    ConfigUtils.complementProperties(sconfig, serverConfig, project.gretty.serverConfig, ServerConfig.getDefault(project))
    sconfig.resolve(project)

    WebAppConfig wconfig = new WebAppConfig()
    ConfigUtils.complementProperties(wconfig, webAppConfig, project.gretty.webAppConfig, WebAppConfig.getDefaultForProject(project), new WebAppConfig(inplace: true))
    
    wconfig.resolve(project, project.gretty.servletContainer)

    new LauncherConfig() {
        
      boolean getDebug() {
        self.debug
      }

      boolean getIntegrationTest() {
        self.getIntegrationTest()
      }
  
      boolean getInteractive() {
        self.getInteractive()
      }

      def getJacocoConfig() {
        self.jacoco
      }

      boolean getManagedClassReload() {
        self.getManagedClassReload()
      }

      ServerConfig getServerConfig() {
        sconfig
      }

      String getServletContainer() {
        self.project.gretty.servletContainer
      }
  
      String getStopTaskName() {
        self.getStopTaskName()
      }

      Iterable<WebAppConfig> getWebAppConfigs() {
        [ wconfig ]
      }
    }
  }
  
  protected boolean getManagedClassReload() {
    project.gretty.managedClassReload
  }

  @Override
  protected String getStopTaskName() {
    'jettyStop'
  }
}

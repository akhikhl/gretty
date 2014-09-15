/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

/**
 * Gradle task for starting jetty
 *
 * @author akhikhl
 */
class AppStartTask extends StartBaseTask {

  @Delegate
  private ServerConfig serverConfig = new ServerConfig()

  @Delegate
  private WebAppConfig webAppConfig = new WebAppConfig()

  protected String getCompatibleServletContainer(String servletContainer) {
    servletContainer
  }

  protected boolean getEffectiveInplace() {
    if(webAppConfig.inplace != null)
      webAppConfig.inplace
    else if(project.gretty.webAppConfig.inplace != null)
      project.gretty.webAppConfig.inplace
    else
      true
  }

  protected String getEffectiveInplaceMode() {
      if(webAppConfig.inplaceMode != null)
        webAppConfig.inplaceMode
      else if (project.gretty.webAppConfig.inplaceMode != null)
        project.gretty.webAppConfig.inplaceMode
      else
        true
  }

  @Override
  protected StartConfig getStartConfig() {

    ServerConfig sconfig = new ServerConfig()
    ConfigUtils.complementProperties(sconfig, serverConfig, project.gretty.serverConfig, ProjectUtils.getDefaultServerConfig(project))
    sconfig.servletContainer = getCompatibleServletContainer(sconfig.servletContainer)
    ProjectUtils.resolveServerConfig(project, sconfig)
    doPrepareServerConfig(sconfig)

    WebAppConfig wconfig = new WebAppConfig()
    ConfigUtils.complementProperties(wconfig, webAppConfig, project.gretty.webAppConfig, ProjectUtils.getDefaultWebAppConfigForProject(project), new WebAppConfig(inplace: true, inplaceMode: 'soft'))
    ProjectUtils.resolveWebAppConfig(project, wconfig, sconfig)
    doPrepareWebAppConfig(wconfig)

    new StartConfig() {

      @Override
      ServerConfig getServerConfig() {
        sconfig
      }

      @Override
      Iterable<WebAppConfig> getWebAppConfigs() {
        [ wconfig ]
      }
    }
  }

  @Override
  protected String getStopCommand() {
    'gradle appStop'
  }

  // use contextConfigFile instead
  @Deprecated
  def getJettyEnvXmlFile() {
    webAppConfig.getJettyEnvXmlFile()
  }

  // use contextConfigFile instead
  @Deprecated
  void setJettyEnvXmlFile(newValue) {
    webAppConfig.setJettyEnvXmlFile(newValue)
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

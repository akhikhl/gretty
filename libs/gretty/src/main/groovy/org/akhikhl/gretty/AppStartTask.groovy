/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

/**
 * Gradle task for starting jetty
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
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
      "soft"
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

    if(wconfig.inplace && wconfig.inplaceMode == 'hard') {
        logger.warn('You\'re running webapp in hard inplaceMode: Overlay and filtering features of gretty will be disabled!')
    }

    if(wconfig.webXml) {
        logger.warn('You\'ve configured gretty to use the web.xml file located at ' + wconfig.webXml + '.')
    }

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

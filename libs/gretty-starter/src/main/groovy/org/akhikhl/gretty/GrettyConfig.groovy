/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
class GrettyConfig {

  @Delegate
  protected ServerConfig serverConfig = new ServerConfig()

  @Delegate
  protected WebAppConfig webAppConfig = new WebAppConfig()

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

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

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
class FarmConfig {

  @Delegate
  protected ServerConfig serverConfig = new ServerConfig()

  // key is project path or war path, value is options
  protected Map webAppRefs_ = [:]

  // use serverConfigFile instead
  @Deprecated
  def getJettyXmlFile() {
    serverConfig.getJettyXmlFile()
  }

  Map getWebAppRefs() {
    webAppRefs_.asImmutable()
  }

  // use serverConfigFile instead
  @Deprecated
  void setJettyXmlFile(newValue) {
    serverConfig.setJettyXmlFile(newValue)
  }

  void webapp(Map options = [:], w) {
    webAppRefs_[w] = options
  }
}

/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */

package org.akhikhl.gretty

import org.gradle.api.Project

/**
 * Describes properties of Gretty product.
 * @author akhikhl
 */
class ProductExtension {

  @Delegate
  protected ServerConfig serverConfig = new ServerConfig()

  // key is project path or war path, value is options
  Map webAppRefs = [:]

  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webAppRefs[w] = options
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


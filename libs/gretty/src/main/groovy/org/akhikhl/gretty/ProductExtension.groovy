/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
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
  //
  boolean includeVersion = false

  /**
   * Additional files that should be included into product build.
   * Key is source file path. Relative path is resolved against build root directory.
   * Value is destination file path relative to the product root directory.
   */
  Map additionalFiles = [:]

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


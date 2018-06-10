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

@CompileStatic(TypeCheckingMode.SKIP)
class FarmConfig {

  @Delegate
  protected final ServerConfig serverConfig

  // key is project path or war path, value is options
  protected final Map webAppRefs_ = [:]

  // list of projects or project paths
  protected final List integrationTestProjects_ = []

  FarmConfig(Map options) {
    serverConfig = options.serverConfig ?: new ServerConfig()
    webAppRefs_ = [:]
    if(options.containsKey('webAppRefs'))
      webAppRefs_ << (options.webAppRefs as Map)
    if(options.containsKey('integrationTestProjects'))
      integrationTestProjects_.addAll(options.integrationTestProjects as Collection)
    if(options.containsKey('integrationTestProject'))
      integrationTestProjects_.add(options.integrationTestProject)
  }

  List getIntegrationTestProjects() {
    integrationTestProjects_.asImmutable()
  }

  // use serverConfigFile instead
  @Deprecated
  def getJettyXmlFile() {
    serverConfig.getJettyXmlFile()
  }

  Map getWebAppRefs() {
    webAppRefs_.asImmutable()
  }

  void integrationTestProject(Object project) {
    integrationTestProjects_.add(project)
  }

  // use serverConfigFile instead
  @Deprecated
  void setJettyXmlFile(newValue) {
    serverConfig.setJettyXmlFile(newValue)
  }

  void setWebAppRefs(Map newValue) {
    if(!webAppRefs_.is(newValue)) {
      webAppRefs_.clear()
      webAppRefs_ << newValue
    }
  }

  void webapp(Map options = [:], w) {
    webAppRefs_[w] = options
  }
}

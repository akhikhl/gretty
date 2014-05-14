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
class GrettyStartFarmTask extends GrettyStartBaseTask {

  List<WebAppRunConfig> webapps = []

  @Override
  List<WebAppRunConfig> getWebApps() {
    webapps
  }

  void webapp(Closure webAppConfigClosure) {
    WebAppRunConfig webapp = new WebAppRunConfig()
    webAppConfigClosure.delegate = webapp
    webAppConfigClosure.resolveStrategy = Closure.DELEGATE_FIRST
    webAppConfigClosure()
  }

  @Override
  protected void setupProperties() {
    serverConfig.setupProperties([project], project.gretty.serverConfig)
    webAppConfig.setupProperties(project, project.gretty.webAppConfig)
    super.setupProperties()
  }
}


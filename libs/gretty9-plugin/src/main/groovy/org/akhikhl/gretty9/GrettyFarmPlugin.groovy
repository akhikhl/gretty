/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty9

import org.akhikhl.gretty.GrettyFarmPluginBase
import org.akhikhl.gretty.ScannerManagerFactory
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class GrettyFarmPlugin extends GrettyFarmPluginBase {

  @Override
  String getJettyVersion() {
    Messages.getString('jettyVersion')
  }

  @Override
  String getPluginName() {
    Messages.getString('farmPluginName')
  }

  @Override
  ScannerManagerFactory getScannerManagerFactory() {
    ScannerManagerFactoryImpl.instance
  }

  @Override
  void injectDependencies(Project project) {
    project.dependencies {
      grettyHelperConfig 'org.akhikhl.gretty:gretty9-helper:0.0.17'
    }
  }
}

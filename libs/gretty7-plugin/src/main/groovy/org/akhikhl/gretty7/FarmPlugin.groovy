/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty7

import org.akhikhl.gretty.FarmPluginBase
import org.akhikhl.gretty.ScannerManagerFactory
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class FarmPlugin extends FarmPluginBase {

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
      grettyHelperConfig 'org.akhikhl.gretty:gretty7-helper:0.0.18'
    }
  }
}

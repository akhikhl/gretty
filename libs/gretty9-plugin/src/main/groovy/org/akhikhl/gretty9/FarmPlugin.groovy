/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty9

import org.akhikhl.gretty.FarmPluginBase
import org.akhikhl.gretty.ScannerManagerFactory
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class FarmPlugin extends FarmPluginBase {

  static final ScannerManagerFactory scannerManagerFactory = new ScannerManagerFactoryImpl()

  @Override
  protected String getJettyVersion() {
    Messages.getString('jettyVersion')
  }

  @Override
  protected String getPluginName() {
    Messages.getString('farmPluginName')
  }

  @Override
  protected ScannerManagerFactory getScannerManagerFactory() {
    scannerManagerFactory
  }

  @Override
  protected void injectDependencies(Project project) {
    project.dependencies {
      grettyHelperConfig 'org.akhikhl.gretty:gretty9-helper:0.0.23'
    }
  }
}

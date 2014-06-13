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

  private final ScannerManagerFactory scannerManagerFactory = new ScannerManagerFactoryImpl()

  private final ResourceBundle messages

  FarmPlugin() {
    messages = ResourceBundle.getBundle('org.akhikhl.gretty9.Messages', Locale.ENGLISH, this.getClass().getClassLoader())
  }

  @Override
  protected String getJettyVersion() {
    messages.getString('jettyVersion')
  }

  @Override
  protected String getPluginName() {
    messages.getString('farmPluginName')
  }

  @Override
  protected ScannerManagerFactory getScannerManagerFactory() {
    scannerManagerFactory
  }

  @Override
  protected void injectDependencies(Project project) {
    project.dependencies {
      grettyHelperConfig 'org.akhikhl.gretty:gretty9-helper:0.0.26'
    }
  }
}

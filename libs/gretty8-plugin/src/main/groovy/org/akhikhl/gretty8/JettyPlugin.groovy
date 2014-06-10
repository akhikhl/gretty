/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty8

import org.gradle.api.Project
import org.akhikhl.gretty.JettyPluginBase
import org.akhikhl.gretty.ScannerManagerFactory

class JettyPlugin extends JettyPluginBase {

  private final ScannerManagerFactory scannerManagerFactory = new ScannerManagerFactoryImpl()

  private final ResourceBundle messages

  JettyPlugin() {
    messages = ResourceBundle.getBundle('org.akhikhl.gretty8.Messages', Locale.ENGLISH, this.getClass().getClassLoader())
  }

  @Override
  protected String getJettyVersion() {
    messages.getString('jettyVersion')
  }

  @Override
  protected String getPluginName() {
    messages.getString('pluginName')
  }

  @Override
  protected ScannerManagerFactory getScannerManagerFactory() {
    scannerManagerFactory
  }

  @Override
  protected void injectDependencies(Project project) {
    project.dependencies {
      providedCompile 'javax.servlet:javax.servlet-api:3.0.1'
      grettyHelperConfig 'org.akhikhl.gretty:gretty8-helper:0.0.24'
    }
  }
}

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty9

import org.gradle.api.Project
import org.akhikhl.gretty.JettyPluginBase
import org.akhikhl.gretty.ScannerManagerFactory

class JettyPlugin extends JettyPluginBase {

  private final ResourceBundle messages

  JettyPlugin() {
    messages = ResourceBundle.getBundle('org.akhikhl.gretty9.Messages', Locale.ENGLISH, this.getClass().getClassLoader())
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
  protected void injectDependencies(Project project) {
    project.dependencies {
      providedCompile 'javax.servlet:javax.servlet-api:3.1.0'
      grettyHelperConfig 'org.akhikhl.gretty:gretty9-helper:0.0.26'
    }
  }
}

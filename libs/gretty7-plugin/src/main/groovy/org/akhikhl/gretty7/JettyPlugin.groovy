/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty7

import org.gradle.api.Project
import org.akhikhl.gretty.JettyPluginBase
import org.akhikhl.gretty.ScannerManagerFactory
import org.akhikhl.gretty7.Messages

final class JettyPlugin extends JettyPluginBase {

  @Override
  String getJettyVersion() {
    Messages.getString('jettyVersion')
  }

  @Override
  String getPluginName() {
    Messages.getString('pluginName')
  }

  @Override
  ScannerManagerFactory getScannerManagerFactory() {
    ScannerManagerFactoryImpl.instance
  }

  @Override
  void injectDependencies(Project project) {
    project.dependencies {
      providedCompile 'javax.servlet:servlet-api:2.5'
      grettyHelperConfig 'org.akhikhl.gretty:gretty7-helper:0.0.17'
    }
  }
}


/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty9

import org.gradle.api.Project
import org.akhikhl.gretty.GrettyPluginBase

class GrettyPlugin extends GrettyPluginBase {

  @Override
  void injectDependencies(Project project) {
    project.dependencies {
      providedCompile 'javax.servlet:javax.servlet-api:3.1.0'
      grettyHelperConfig 'org.akhikhl.gretty:gretty9-helper:0.0.17'
    }
  }
}

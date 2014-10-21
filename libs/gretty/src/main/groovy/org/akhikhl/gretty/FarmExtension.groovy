/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class FarmExtension extends FarmConfig {

  String integrationTestTask = 'integrationTest'

  List includes = []

  protected afterEvaluate = []

  void afterEvaluate(Closure closure) {
    afterEvaluate.add(closure)
  }

  Map getWebAppRefs(Project project) {
    if(includes) {
      Map result = [:] << webAppRefs
      for(def include in includes)
        result << project.project(include.project).farms[farmName].getWebAppRefs(project)
      result
    } else
      webAppRefs
  }

  void include(def project, def farmName = '') {
    includes.add([ project: project, farmName: farmName ])
  }

  @Override
  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webAppRefs[w] = options
  }
}

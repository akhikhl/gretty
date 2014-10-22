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

  final Project project

  String integrationTestTask = 'integrationTest'

  private final List includes_ = []

  private final List afterEvaluate_ = []

  FarmExtension(Map options = [:], Project project) {
    this.project = project
    for(def e in options) {
      def key = e.key
      if(key == 'webAppRefs')
        key = 'webAppRefs_'
      else if(key == 'includes')
        key = 'includes_'
      this[key] = e.value
    }
  }

  void afterEvaluate(Closure closure) {
    afterEvaluate_.add(closure)
  }

  List getAfterEvaluate() {
    afterEvaluate_.asImmutable()
  }

  List getIncludes() {
    includes.asImmutable()
  }

  @Override
  Map getWebAppRefs() {
    if(includes_) {
      Map result = [:] << webAppRefs_
      for(Map include in includes_) {
        def otherFarms = project.project(include.project).extensions.findByName('farms')
        if(otherFarms)
          result << otherFarms.farmsMap[include.farmName].webAppRefs
      }
      result.asImmutable()
    } else
      webAppRefs_.asImmutable()
  }

  void include(def project, def farmName = '') {
    includes_.add([ project: project, farmName: farmName ])
  }

  @Override
  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webAppRefs_[w] = options
  }
}

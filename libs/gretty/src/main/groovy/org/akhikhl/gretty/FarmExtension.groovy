/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class FarmExtension extends FarmConfig {

  String integrationTestTask = 'integrationTest'

  protected afterEvaluate = []

  void afterEvaluate(Closure closure) {
    afterEvaluate.add(closure)
  }

  @Override
  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webAppRefs[w] = options
  }
}

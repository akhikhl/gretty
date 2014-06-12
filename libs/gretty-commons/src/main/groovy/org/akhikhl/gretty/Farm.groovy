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
class Farm {

  @Delegate
  protected ServerConfig serverConfig = new ServerConfig()

  // key is project path or war path, value is options
  Map webAppRefs = [:]

  String integrationTestTask = 'integrationTest'

  protected afterEvaluate = []

  void afterEvaluate(Closure closure) {
    afterEvaluate.add(closure)
  }

  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webAppRefs[w] = options
  }
}

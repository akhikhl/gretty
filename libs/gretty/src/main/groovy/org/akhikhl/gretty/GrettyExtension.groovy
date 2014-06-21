/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.GradleException

class GrettyExtension {

  @Delegate
  protected ServerConfig serverConfig = new ServerConfig()

  @Delegate
  protected WebAppConfig webAppConfig = new WebAppConfig()

  protected List overlays = []

  String integrationTestTask = 'integrationTest'

  protected afterEvaluate = []

  void afterEvaluate(Closure closure) {
    afterEvaluate.add(closure)
  }

  void overlay(def newValue) {
    if(!(newValue instanceof String))
      throw new GradleException("Overlay ${newValue?.toString()} should be a string")
    overlays.add newValue
  }
}

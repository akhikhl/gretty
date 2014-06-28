/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
class GrettyExtension extends GrettyConfig {

  protected List overlays = []

  String integrationTestTask = 'integrationTest'

  protected afterEvaluate = []

  void afterEvaluate(Closure closure) {
    afterEvaluate.add(closure)
  }

  void overlay(def newValue) {
    if(!(newValue instanceof String))
      throw new Exception("Overlay ${newValue?.toString()} should be a string")
    overlays.add newValue
  }
}

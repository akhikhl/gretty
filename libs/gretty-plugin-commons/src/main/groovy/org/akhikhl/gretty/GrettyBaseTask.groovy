/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException

/**
 *
 * @author akhikhl
 */
abstract class GrettyBaseTask extends DefaultTask {

  GrettyBaseTask() {
    doFirst() {
      setupProperties()
    }
    doLast {
      action()
    }
  }

  abstract protected void action()

  protected final void requiredProperty(String propName) {
    if(!hasProperty(propName) || !properties[propName])
      throw new GradleException("Missing required property: ${getClass().getName()}.${propName}")
  }

  protected void setupProperties() {
  }
}

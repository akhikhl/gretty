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
 * Base class for all gretty tasks
 *
 * @author akhikhl
 */
abstract class GrettyBaseTask extends DefaultTask {

  protected propertiesResolved = false

  GrettyBaseTask() {
    doFirst() {
      if(!propertiesResolved)
        resolveProperties()
    }
    doLast {
      action()
    }
  }

  abstract protected void action()

  protected final void requireAnyProperty(String... propNames) {
    for(String propName in propNames) {
      if(hasProperty(propName) && properties[propName])
        return
    }
    throw new GradleException("Missing at least one of the required properties ${propNames} in ${getClass().getName()}")
  }

  protected final void requireProperty(String propName) {
    if(!hasProperty(propName) || !properties[propName])
      throw new GradleException("Missing required property '${propName}' in ${getClass().getName()}")
  }

  protected void resolveProperties() {
    propertiesResolved = true
  }
}

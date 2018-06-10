/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class GradleUtils {
  
  static boolean derivedFrom(Class targetClass, String className) {
    while(targetClass != null) {
      if(targetClass.getName() == className)
        return true
      for(Class intf in targetClass.getInterfaces())
        if(derivedFrom(intf, className))
          return true
      targetClass = targetClass.getSuperclass()
    }
    return false
  }
  
  /**
   * Replacement for instanceof operator, workaround for Gradle 1.10 bug:
   * task classes defined in "build.gradle" fail instanceof check for base classes in gradle plugins.
   */
  static boolean instanceOf(Object obj, String className) {
    derivedFrom(obj.getClass(), className)
  }

  static void disableTaskOnOtherProjects(Project thisProject, String taskName) {
    thisProject.rootProject.allprojects { proj ->
      if(proj != thisProject && proj.tasks.findByName(taskName))
        proj.tasks[taskName].enabled = false
    }
  }
}


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
class GradleUtils {

  static void disableTaskOnOtherProjects(Project thisProject, String taskName) {
    thisProject.rootProject.allprojects { proj ->
      if(proj != thisProject && proj.tasks.findByName(taskName))
        proj.tasks[taskName].enabled = false
    }
  }
}


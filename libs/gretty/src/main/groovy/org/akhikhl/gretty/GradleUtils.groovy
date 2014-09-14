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
class GradleUtils {

  static void disableTaskOnOtherProjects(Project thisProject, String taskName) {
    thisProject.rootProject.allprojects { proj ->
      if(proj != thisProject && proj.tasks.findByName(taskName))
        proj.tasks[taskName].enabled = false
    }
  }
}


/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class GrettyFarmPlugin implements Plugin<Project> {

  void apply(final Project project) {

    if(project.extensions.findByName('farm'))
      return // plugin is already applied

    project.extensions.create('farm', Farm)

    project.extensions.create('farms', Farms)
    project.farms.farmsMap[''] = project.farm

    project.afterEvaluate {

      project.farms.farmsMap.keySet().each { fname ->

        project.task('jettyRunFarm' + fname, type: GrettyStartFarmTask, group: 'gretty') {
          description = 'Starts web-apps farm inplace, in interactive mode (keypress stops the server).'
          farmName = fname
        }
      }
    }
  }
}

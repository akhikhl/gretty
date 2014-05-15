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
class GrettyFarmPluginBase implements Plugin<Project> {

  void apply(final Project project) {

    project.extensions.create('farm', Farm)

    project.extensions.create('farms', Farms)
    projects.farms.farmsMap[''] = project.farm

    project.afterEvaluate {

      farmsMap.keySet().each { fname ->

        task('jettyRunFarm' + fname, type: GrettyStartFarmTask, group: 'gretty') { thisTask ->
          description = 'Starts web-apps farm inplace, in interactive mode (keypress stops the server).'
          farmName = fname
        }
      }
    }
  }
}


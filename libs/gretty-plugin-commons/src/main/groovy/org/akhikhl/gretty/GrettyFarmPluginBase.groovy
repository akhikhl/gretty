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

      farmsMap.each { farmName_, farm_ ->

        task('jettyRunFarm' + farmName_, type: GrettyStartFarmTask, group: 'gretty') { thisTask ->
          description = 'Starts web-apps farm inplace, in interactive mode (keypress stops the server).'
          farm_.webapps.each { webapp ->
            thisTask.dependsOn { project.project(webapp).tasks.prepareInplaceWebApp }
          }
          farmName = farmName_
          farm = farm_
        }
      }
    }
  }
}


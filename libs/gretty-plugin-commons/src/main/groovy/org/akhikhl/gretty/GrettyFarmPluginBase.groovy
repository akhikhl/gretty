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
abstract class GrettyFarmPluginBase implements Plugin<Project> {

  void apply(final Project project) {

    if(project.ext.has('grettyFarmPluginName')) {
      log.warn 'You already applied {} to the project {}, so {} is ignored', project.ext.grettyFarmPluginName, project.name, getPluginName()
      return // plugin is already applied
    }

    // TODO: check for jetty versions

    project.ext.grettyFarmPluginName = getPluginName()
    project.ext.jettyVersion = getJettyVersion()

    project.ext.scannerManagerFactory = getScannerManagerFactory()

    project.extensions.create('farm', Farm)

    project.extensions.create('farms', Farms)
    project.farms.farmsMap[''] = project.farm

    if(!project.configurations.findByName('grettyHelperConfig'))
      project.configurations {
        grettyHelperConfig
        grettyLoggingConfig
        gretty.extendsFrom(grettyHelperConfig)
      }

    injectDependencies(project)

    project.afterEvaluate {

      if(!project.repositories)
        project.repositories {
          mavenLocal()
          jcenter()
          mavenCentral()
        }

      project.farms.farmsMap.keySet().each { fname ->

        project.task('jettyRunFarm' + fname, type: GrettyStartFarmTask, group: 'gretty') {
          description = 'Starts web-apps farm inplace, in interactive mode (keypress stops the server).'
          farmName = fname
          dependsOn {
            resolveProperties()
            webApps.collect {
              def proj = project.project(it.projectPath)
              (it.inplace == null || it.inplace) ? proj.tasks.prepareInplaceWebApp : proj.tasks.prepareWarWebApp
            }
          }
        }
      }
    }
  }

  abstract String getJettyVersion()

  abstract String getPluginName()

  abstract ScannerManagerFactory getScannerManagerFactory()

  abstract void injectDependencies(Project project)
}

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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
abstract class FarmPluginBase implements Plugin<Project> {

  private static final Logger log = LoggerFactory.getLogger(FarmPluginBase)

  void apply(final Project project) {

    if(project.ext.has('grettyFarmPluginName')) {
      log.warn 'You already applied {} to the project {}, so {} is ignored', project.ext.grettyFarmPluginName, project.name, getPluginName()
      return
    }

    project.ext.grettyFarmPluginName = getPluginName()
    project.ext.jettyVersion = project.ext.grettyFarmPluginJettyVersion = getJettyVersion()

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

    if(!project.tasks.findByName('run'))
      project.task('run')

    if(!project.tasks.findByName('debug'))
      project.task('debug')

    project.tasks.whenObjectAdded { task ->
      if(task instanceof FarmStartTask)
        task.dependsOn {
          task.getWebAppConfigsForProjects().collect {
            def proj = project.project(it.projectPath)
            boolean inplace = it.inplace == null ? task.inplace : it.inplace
            inplace ? proj.tasks.prepareInplaceWebApp : proj.tasks.prepareWarWebApp
          }
        }
    }

    project.afterEvaluate {

      if(!project.repositories)
        project.repositories {
          mavenLocal()
          jcenter()
          mavenCentral()
        }

      project.farms.farmsMap.each { fname, farm ->

        String farmDescr = fname ? "farm '${fname}'" : 'default farm'

        project.task('farmRun' + fname, type: FarmStartTask, group: 'gretty') {
          description = "Starts ${farmDescr} inplace, in interactive mode."
          farmName = fname
          if(!fname)
            doFirst {
              GradleUtils.disableTaskOnOtherProjects(project, 'run')
              GradleUtils.disableTaskOnOtherProjects(project, 'jettyRun')
              GradleUtils.disableTaskOnOtherProjects(project, 'farmRun')
            }
        }

        if(!fname)
          project.tasks.run.dependsOn 'farmRun'

        project.task('farmRunDebug' + fname, type: FarmStartTask, group: 'gretty') {
          description = "Starts ${farmDescr} inplace, in debug and in interactive mode."
          farmName = fname
          debug = true
          if(!fname)
            doFirst {
              GradleUtils.disableTaskOnOtherProjects(project, 'debug')
              GradleUtils.disableTaskOnOtherProjects(project, 'jettyRunDebug')
              GradleUtils.disableTaskOnOtherProjects(project, 'farmRunDebug')
            }
        }

        if(!fname)
          project.tasks.debug.dependsOn 'farmRunDebug'

        project.task('farmRunWar' + fname, type: FarmStartTask, group: 'gretty') {
          description = "Starts ${farmDescr} on WAR-files, in interactive mode."
          farmName = fname
          inplace = false
        }

        project.task('farmRunWarDebug' + fname, type: FarmStartTask, group: 'gretty') {
          description = "Starts ${farmDescr} on WAR-files, in debug and in interactive mode."
          farmName = fname
          debug = true
          inplace = false
        }

        project.task('farmStart' + fname, type: FarmStartTask, group: 'gretty') {
          description = "Starts ${farmDescr} inplace (stopped by 'farmStop')."
          farmName = fname
          interactive = false
          stopTask = 'farmStop' + fname
        }

        project.task('farmStartDebug' + fname, type: FarmStartTask, group: 'gretty') {
          description = "Starts ${farmDescr} inplace, in debug mode (stopped by 'farmStop')."
          farmName = fname
          interactive = false
          debug = true
          stopTask = 'farmStop' + fname
        }

        project.task('farmStartWar' + fname, type: FarmStartTask, group: 'gretty') {
          description = "Starts ${farmDescr} on WAR-files (stopped by 'farmStop')."
          farmName = fname
          interactive = false
          inplace = false
          stopTask = 'farmStop' + fname
        }

        project.task('farmStartWarDebug' + fname, type: FarmStartTask, group: 'gretty') {
          description = "Starts ${farmDescr} on WAR-files, in debug (stopped by 'farmStop')."
          farmName = fname
          interactive = false
          debug = true
          inplace = false
          stopTask = 'farmStop' + fname
        }

        project.task('farmStop' + fname, type: FarmStopTask, group: 'gretty') {
          description = "Sends \'stop\' command to a running ${farmDescr}."
          farmName = fname
        }

        project.task('farmRestart' + fname, type: FarmRestartTask, group: 'gretty') {
          description = "Sends \'restart\' command to a running ${farmDescr}."
          farmName = fname
        }

        if(farm.integrationTestTask) {

          project.task('farmBeforeIntegrationTest' + fname, type: FarmBeforeIntegrationTestTask, group: 'gretty') {
            description = "Starts ${farmDescr} before integration test."
            farmName = fname
            setupIntegrationTestTaskDependencies()
          }

          project.task('farmAfterIntegrationTest' + fname, type: FarmAfterIntegrationTestTask, group: 'gretty') {
            description = "Stops ${farmDescr} after integration test."
            farmName = fname
            setupIntegrationTestTaskDependencies()
          }
        } // integrationTestTask

      } // farmsMap
    } // afterEvaluate
  } // apply

  abstract String getJettyVersion()

  abstract String getPluginName()

  abstract ScannerManagerFactory getScannerManagerFactory()

  abstract void injectDependencies(Project project)
}

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.*

/**
 *
 * @author akhikhl
 */
abstract class GrettyPluginBase implements Plugin<Project> {

  void apply(final Project project) {

    if (!project.plugins.findPlugin(org.gradle.api.plugins.WarPlugin))
      project.apply(plugin: org.gradle.api.plugins.WarPlugin)

    project.extensions.create('gretty', GrettyPluginExtension)

    project.configurations {
      grettyHelperConfig
      grettyLoggingConfig
	    gretty.extendsFrom(grettyHelperConfig)
    }

    injectDependencies(project)

    project.task('run')
    project.task('debug')

    def self = this
    project.ext._createScannerManager = {
      self.createScannerManager()
    }

    project.afterEvaluate {
      for(String overlay in project.gretty.overlays)
        project.dependencies.add 'providedCompile', project.project(overlay)

      project.task('prepareInplaceWebAppFolder', group: 'gretty') {
        description = 'Copies webAppDir of this web-application and all WAR-overlays (if any) to ${buildDir}/inplaceWebapp'
        inputs.dir project.webAppDir
        outputs.dir "${project.buildDir}/inplaceWebapp"
        doLast {
          ProjectUtils.prepareInplaceWebAppFolder(project)
        }
      }

      project.task('prepareInplaceWebAppClasses', group: 'gretty') {
        description = 'Compiles classes of this web-application and all WAR-overlays (if any)'
        dependsOn project.tasks.classes
        for(String overlay in project.gretty.overlays)
          dependsOn "$overlay:prepareInplaceWebAppClasses"
      }

      project.task('prepareInplaceWebApp', group: 'gretty') {
        description = 'Prepares inplace web-application'
        dependsOn project.tasks.prepareInplaceWebAppFolder
        dependsOn project.tasks.prepareInplaceWebAppClasses
      }

      if(project.gretty.overlays) {

        project.ext.finalWarPath = project.tasks.war.archivePath

        project.tasks.war.archiveName = 'partialWar.war'

        // 'explodeWebApps' task is only activated by 'overlayWar' task
        project.task('explodeWebApps', group: 'gretty') {
          description = 'Explodes this web-application and all WAR-overlays (if any) to ${buildDir}/explodedWebapp'
          for(String overlay in project.gretty.overlays)
            dependsOn "$overlay:assemble" as String
          dependsOn project.tasks.war
          for(String overlay in project.gretty.overlays)
            inputs.file { ProjectUtils.getFinalWarPath(project.project(overlay)) }
          inputs.file project.tasks.war.archivePath
          outputs.dir "${project.buildDir}/explodedWebapp"
          doLast {
            ProjectUtils.prepareExplodedWebAppFolder(project)
          }
        }

        project.task('overlayWar', group: 'gretty') {
          description = 'Creates WAR from exploded web-application in ${buildDir}/explodedWebapp'
          dependsOn project.tasks.explodeWebApps
          inputs.dir "${project.buildDir}/explodedWebapp"
          outputs.file project.ext.finalWarPath
          doLast {
            ant.zip destfile: project.ext.finalWarPath, basedir: "${project.buildDir}/explodedWebapp"
          }
        }

        project.tasks.assemble.dependsOn project.tasks.overlayWar
      } // overlays

      project.task('prepareWarWebApp', group: 'gretty') {
        description = 'Prepares war web-application'
        if(project.gretty.overlays)
          dependsOn project.tasks.overlayWar
        else
          dependsOn project.tasks.war
      }

      project.ext.executorService = Executors.newSingleThreadExecutor()

      project.task('jettyRun', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts jetty server inplace, in interactive mode (keypress stops the server).'
        dependsOn project.tasks.prepareInplaceWebApp
      }

      project.tasks.run.dependsOn 'jettyRun'

      project.task('jettyRunDebug', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts jetty server inplace, in debug and interactive mode (keypress stops the server).'
        dependsOn project.tasks.prepareInplaceWebApp
        debug = true
      }

      project.tasks.debug.dependsOn 'jettyRunDebug'

      project.task('jettyRunWar', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts jetty server on WAR-file, in interactive mode (keypress stops the server).'
        dependsOn project.tasks.prepareWarWebApp
        inplace = false
      }

      project.task('jettyRunWarDebug', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts jetty server on WAR-file, in debug and interactive mode (keypress stops the server).'
        dependsOn project.tasks.prepareWarWebApp
        inplace = false
        debug = true
      }

      project.task('jettyStart', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts jetty server inplace, in batch mode (\'jettyStop\' stops the server).'
        dependsOn project.tasks.prepareInplaceWebApp
        interactive = false
      }

      project.task('jettyStartDebug', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts jetty server inplace, in debug and batch mode (\'jettyStop\' stops the server).'
        dependsOn project.tasks.prepareInplaceWebApp
        interactive = false
        debug = true
      }

      project.task('jettyStartWar', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts jetty server on WAR-file, in batch mode (\'jettyStop\' stops the server).'
        dependsOn project.tasks.prepareWarWebApp
        inplace = false
        interactive = false
      }

      project.task('jettyStartWarDebug', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts jetty server on WAR-file, in debug and batch mode (\'jettyStop\' stops the server).'
        dependsOn project.tasks.prepareWarWebApp
        inplace = false
        interactive = false
        debug = true
      }

      project.task('jettyStop', type: GrettyServiceTask, group: 'gretty') {
        description = 'Sends \'stop\' command to running jetty server.'
        command = 'stop'
      }

      project.task('jettyRestart', type: GrettyServiceTask, group: 'gretty') {
        description = 'Sends \'restart\' command to running jetty server.'
        command = 'restart'
      }

      if(project.gretty.integrationTestTask) {

        project.task('jettyBeforeIntegrationTest', type: GrettyStartTask, group: 'gretty') {
          description = 'Starts jetty server before integration test.'
          dependsOn project.tasks.prepareInplaceWebApp
          dependsOn project.tasks.testClasses
          project.tasks[project.gretty.integrationTestTask].dependsOn it
          integrationTest = true
        }

        project.task('jettyAfterIntegrationTest', type: GrettyServiceTask, group: 'gretty') {
          description = 'Stops jetty server after integration test.'
          project.tasks[project.gretty.integrationTestTask].finalizedBy it
          command = 'stop'
        }
      } // integrationTestTask
    } // afterEvaluate
  } // apply

  abstract ScannerManagerBase createScannerManager()

  abstract void injectDependencies(Project project)

} // GrettyPluginBase

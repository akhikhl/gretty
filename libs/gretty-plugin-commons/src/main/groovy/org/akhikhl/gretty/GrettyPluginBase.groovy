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
abstract class GrettyPluginBase implements Plugin<Project> {

  private static final Logger log = LoggerFactory.getLogger(GrettyPluginBase)

  void apply(final Project project) {

    if(project.ext.has('grettyPluginName')) {
      log.warn 'You already applied {} to the project {}, so {} is ignored', project.ext.grettyPluginName, project.name, getPluginName()
      return
    }

    if (!project.plugins.findPlugin(org.gradle.api.plugins.WarPlugin))
      project.apply(plugin: org.gradle.api.plugins.WarPlugin)

    project.ext.grettyPluginName = getPluginName()
    project.ext.jettyVersion = project.ext.grettyPluginJettyVersion = getJettyVersion()

    project.ext.scannerManagerFactory = getScannerManagerFactory()

    project.extensions.create('gretty', GrettyExtension)

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

    project.afterEvaluate {

      if(!project.repositories)
        project.repositories {
          mavenLocal()
          jcenter()
          mavenCentral()
        }

      for(String overlay in project.gretty.overlays)
        project.dependencies.add 'providedCompile', project.project(overlay)

      project.task('prepareInplaceWebAppFolder', group: 'gretty') {
        description = 'Copies webAppDir of this web-app and all WAR-overlays (if any) to ${buildDir}/inplaceWebapp'
        inputs.dir project.webAppDir
        outputs.dir "${project.buildDir}/inplaceWebapp"
        doLast {
          ProjectUtils.prepareInplaceWebAppFolder(project)
        }
      }

      project.task('prepareInplaceWebAppClasses', group: 'gretty') {
        description = 'Compiles classes of this web-app and all WAR-overlays (if any)'
        dependsOn project.tasks.classes
        for(String overlay in project.gretty.overlays)
          dependsOn "$overlay:prepareInplaceWebAppClasses"
      }

      project.task('prepareInplaceWebApp', group: 'gretty') {
        description = 'Prepares inplace web-app'
        dependsOn project.tasks.prepareInplaceWebAppFolder
        dependsOn project.tasks.prepareInplaceWebAppClasses
      }

      if(project.gretty.overlays) {

        project.ext.finalWarPath = project.tasks.war.archivePath

        project.tasks.war.archiveName = 'partialWar.war'

        // 'explodeWebApps' task is only activated by 'overlayWar' task
        project.task('explodeWebApps', group: 'gretty') {
          description = 'Explodes this web-app and all WAR-overlays (if any) to ${buildDir}/explodedWebapp'
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
          description = 'Creates WAR from exploded web-app in ${buildDir}/explodedWebapp'
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
        description = 'Prepares war web-app'
        if(project.gretty.overlays)
          dependsOn project.tasks.overlayWar
        else
          dependsOn project.tasks.war
      }

      project.task('jettyRun', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in interactive mode.'
        dependsOn project.tasks.prepareInplaceWebApp
        doFirst {
          GradleUtils.disableTaskOnOtherProjects(project, 'run')
        }
      }

      // As soon as farm plugin applies to the same project, it takes over run task.
      if(!project.ext.has('grettyFarmPluginName'))
        project.tasks.run.dependsOn 'jettyRun'

      project.task('jettyRunDebug', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in debug and interactive mode.'
        dependsOn project.tasks.prepareInplaceWebApp
        debug = true
        doFirst {
          GradleUtils.disableTaskOnOtherProjects(project, 'debug')
        }
      }

      // As soon as farm plugin applies to the same project, it takes over debug task.
      if(!project.ext.has('grettyFarmPluginName'))
        project.tasks.debug.dependsOn 'jettyRunDebug'

      project.task('jettyRunWar', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts web-app on WAR-file, in interactive mode.'
        dependsOn project.tasks.prepareWarWebApp
        inplace = false
      }

      project.task('jettyRunWarDebug', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts web-app on WAR-file, in debug and interactive mode.'
        dependsOn project.tasks.prepareWarWebApp
        inplace = false
        debug = true
      }

      project.task('jettyStart', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts web-app inplace (stopped by \'jettyStop\').'
        dependsOn project.tasks.prepareInplaceWebApp
        interactive = false
      }

      project.task('jettyStartDebug', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in debug mode (stopped by \'jettyStop\').'
        dependsOn project.tasks.prepareInplaceWebApp
        interactive = false
        debug = true
      }

      project.task('jettyStartWar', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts web-app on WAR-file (stopped by \'jettyStop\').'
        dependsOn project.tasks.prepareWarWebApp
        inplace = false
        interactive = false
      }

      project.task('jettyStartWarDebug', type: GrettyStartTask, group: 'gretty') {
        description = 'Starts web-app on WAR-file, in debug mode (stopped by \'jettyStop\').'
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

  abstract String getJettyVersion()

  abstract String getPluginName()

  abstract ScannerManagerFactory getScannerManagerFactory()

  abstract void injectDependencies(Project project)

} // GrettyPluginBase

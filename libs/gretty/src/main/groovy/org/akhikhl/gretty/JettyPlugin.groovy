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
import org.gradle.api.tasks.testing.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class JettyPlugin implements Plugin<Project> {

  protected static final Logger log = LoggerFactory.getLogger(JettyPlugin)

  void apply(final Project project) {

    if(project.ext.has('jettyPluginApplied')) {
      log.warn 'You already applied JettyPlugin to the project {}, so second apply is ignored', project.name
      return
    }

    project.ext.jettyPluginApplied = true

    project.ext.launcherFactory = getLauncherFactory()

    project.extensions.create('gretty', GrettyExtension)

    createConfigurations(project)

    if(!project.tasks.findByName('run'))
      project.task('run')

    if(!project.tasks.findByName('debug'))
      project.task('debug')

    project.tasks.whenObjectAdded { task ->
      if(task instanceof JettyStartTask)
        task.dependsOn {
          task.effectiveInplace ? project.tasks.prepareInplaceWebApp : project.tasks.prepareArchiveWebApp
        }
    }

    project.afterEvaluate {

      injectDefaultRepositories(project)
      injectDependencies(project)

      for(String overlay in project.gretty.overlays)
        project.dependencies.add 'providedCompile', project.project(overlay)

      project.task('prepareInplaceWebAppFolder', group: 'gretty') {
        description = 'Copies webAppDir of this web-app and all overlays (if any) to ${buildDir}/inplaceWebapp'
        inputs.dir ProjectUtils.getWebAppDir(project)
        outputs.dir "${project.buildDir}/inplaceWebapp"
        doLast {
          ProjectUtils.prepareInplaceWebAppFolder(project)
        }
      }

      project.task('prepareInplaceWebAppClasses', group: 'gretty') {
        description = 'Compiles classes of this web-app and all overlays (if any)'
        dependsOn project.tasks.classes
        for(String overlay in project.gretty.overlays)
          dependsOn "$overlay:prepareInplaceWebAppClasses"
      }

      project.task('prepareInplaceWebApp', group: 'gretty') {
        description = 'Prepares inplace web-app'
        dependsOn project.tasks.prepareInplaceWebAppFolder
        dependsOn project.tasks.prepareInplaceWebAppClasses
      }

      def archiveTask = project.tasks.findByName('war') ?: project.tasks.jar

      if(project.gretty.overlays) {

        project.ext.finalArchivePath = archiveTask.archivePath

        archiveTask.archiveName = 'partial.' + (project.tasks.findByName('war') ? 'war' : 'jar')

        // 'explodeWebApps' task is only activated by 'overlayArchive' task
        project.task('explodeWebApps', group: 'gretty') {
          description = 'Explodes this web-app and all overlays (if any) to ${buildDir}/explodedWebapp'
          for(String overlay in project.gretty.overlays)
            dependsOn "$overlay:assemble" as String
          dependsOn archiveTask
          for(String overlay in project.gretty.overlays)
            inputs.file { ProjectUtils.getFinalArchivePath(project.project(overlay)) }
          inputs.file archiveTask.archivePath
          outputs.dir "${project.buildDir}/explodedWebapp"
          doLast {
            ProjectUtils.prepareExplodedWebAppFolder(project)
          }
        }

        project.task('overlayArchive', group: 'gretty') {
          description = 'Creates archive from exploded web-app in ${buildDir}/explodedWebapp'
          dependsOn project.tasks.explodeWebApps
          inputs.dir "${project.buildDir}/explodedWebapp"
          outputs.file project.ext.finalArchivePath
          doLast {
            ant.zip destfile: project.ext.finalArchivePath, basedir: "${project.buildDir}/explodedWebapp"
          }
        }

        project.tasks.assemble.dependsOn project.tasks.overlayArchive
      } // overlays

      project.task('prepareArchiveWebApp', group: 'gretty') {
        description = 'Prepares war web-app'
        if(project.gretty.overlays)
          dependsOn project.tasks.overlayArchive
        else
          dependsOn archiveTask
      }

      project.task('jettyRun', type: JettyStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in interactive mode.'
      }

      // As soon as farm plugin applies to the same project, it takes over run task.
      if(!project.ext.has('grettyFarmPluginName'))
        project.tasks.run.dependsOn 'jettyRun'

      project.task('jettyRunDebug', type: JettyStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in debug and interactive mode.'
        debug = true
      }

      // As soon as farm plugin applies to the same project, it takes over debug task.
      if(!project.ext.has('grettyFarmPluginName'))
        project.tasks.debug.dependsOn 'jettyRunDebug'

      project.task('jettyStart', type: JettyStartTask, group: 'gretty') {
        description = 'Starts web-app inplace (stopped by \'jettyStop\').'
        interactive = false
      }

      project.task('jettyStartDebug', type: JettyStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in debug mode (stopped by \'jettyStop\').'
        interactive = false
        debug = true
      }

      if(project.plugins.findPlugin(org.gradle.api.plugins.WarPlugin)) {
        project.task('jettyRunWar', type: JettyStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file, in interactive mode.'
          inplace = false
        }

        project.task('jettyRunWarDebug', type: JettyStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file, in debug and interactive mode.'
          inplace = false
          debug = true
        }

        project.task('jettyStartWar', type: JettyStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file (stopped by \'jettyStop\').'
          inplace = false
          interactive = false
        }

        project.task('jettyStartWarDebug', type: JettyStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file, in debug mode (stopped by \'jettyStop\').'
          inplace = false
          interactive = false
          debug = true
        }
      }

      project.task('jettyStop', type: JettyStopTask, group: 'gretty') {
        description = 'Sends \'stop\' command to running jetty server.'
      }

      project.task('jettyRestart', type: JettyRestartTask, group: 'gretty') {
        description = 'Sends \'restart\' command to running jetty server.'
      }

      project.task('jettyBeforeIntegrationTest', type: JettyBeforeIntegrationTestTask, group: 'gretty') {
        description = 'Starts jetty server before integration test.'
      }

      project.task('jettyAfterIntegrationTest', type: JettyAfterIntegrationTestTask, group: 'gretty') {
        description = 'Stops jetty server after integration test.'
      }

      for(Closure afterEvaluateClosure in project.gretty.afterEvaluate) {
        afterEvaluateClosure.delegate = project.gretty
        afterEvaluateClosure.resolveStrategy = Closure.DELEGATE_FIRST
        afterEvaluateClosure()
      }

      if(!project.tasks.jettyBeforeIntegrationTest.integrationTestTaskAssigned)
        project.tasks.jettyBeforeIntegrationTest.integrationTestTask null // default binding

      if(!project.tasks.jettyAfterIntegrationTest.integrationTestTaskAssigned)
        project.tasks.jettyAfterIntegrationTest.integrationTestTask null // default binding

    } // afterEvaluate
  } // apply

  private void createConfigurations(Project project) {
    if(!project.configurations.findByName('grettyHelperConfig'))
      project.configurations {
        grettyHelperConfig
        grettyLoggingConfig
        gretty.extendsFrom(grettyHelperConfig)
        grettyUtil7
        grettyUtil8
        grettyUtil9
      }
    if(!project.configurations.findByName('providedCompile'))
      project.configurations {
        providedCompile
        compile.extendsFrom(providedCompile)
        grettyUtil7 'org.akhikhl.gretty:gretty-util7'
        grettyUtil8 'org.akhikhl.gretty:gretty-util8'
        grettyUtil9 'org.akhikhl.gretty:gretty-util9'
      }
  }

  private LauncherFactory getLauncherFactory() {
    new DefaultLauncherFactory()
  }

  private void injectDefaultRepositories(Project project) {
    project.repositories {
      mavenLocal()
      jcenter()
      mavenCentral()
    }
  }

  private void injectDependencies(Project project) {
    
  }
  
} // JettyPluginBase

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonBuilder
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.*

abstract class GrettyPluginBase implements Plugin<Project> {

  void apply(final Project project) {

    project.extensions.create('gretty', GrettyPluginExtension)

    project.configurations {
      grettyHelperConfig
    }

    injectDependencies(project)

    project.task('run')
    project.task('debug')

    project.afterEvaluate {
      for(String overlay in project.gretty.overlays)
        project.dependencies.add 'providedCompile', project.project(overlay)

      project.task('prepareInplaceWebAppFolder', group: 'gretty', description: 'Copies webAppDir of this web-application and all WAR-overlays (if any) to ${buildDir}/inplaceWebapp') {
        for(String overlay in project.gretty.overlays)
          inputs.dir { project.project(overlay).webAppDir }
        inputs.dir project.webAppDir
        outputs.dir "${project.buildDir}/inplaceWebapp"
        doLast {
          ProjectUtils.prepareInplaceWebAppFolder(project)
        }
      }

      if(project.gretty.overlays) {

        project.ext.finalWarPath = project.tasks.war.archivePath

        project.tasks.war.archiveName = 'partialWar.war'

        // 'explodeWebApps' task is only activated by 'overlayWar' task
        project.task('explodeWebApps', group: 'gretty', description: 'Explodes this web-application and all WAR-overlays (if any) to ${buildDir}/explodedWebapp') {
          for(String overlay in project.gretty.overlays)
            dependsOn "$overlay:war" as String
          dependsOn project.tasks.war
          for(String overlay in project.gretty.overlays)
            inputs.file { ProjectUtils.getFinalWarPath(project.project(overlay)) }
          inputs.file project.tasks.war.archivePath
          outputs.dir "${project.buildDir}/explodedWebapp"
          doLast {
            ProjectUtils.prepareExplodedWebAppFolder(project)
          }
        }

        project.task('overlayWar', group: 'gretty', description: 'Creates WAR from exploded web-application in ${buildDir}/explodedWebapp') {
          dependsOn project.tasks.explodeWebApps
          inputs.dir "${project.buildDir}/explodedWebapp"
          outputs.file project.ext.finalWarPath
          doLast {
            ant.zip destfile: project.ext.finalWarPath,  basedir: "${project.buildDir}/explodedWebapp"
          }
        }

        project.tasks.assemble.dependsOn project.tasks.overlayWar
      } // overlays

      def setupInplaceWebAppDependencies = { task ->
        task.dependsOn project.tasks.classes
        task.dependsOn project.tasks.prepareInplaceWebAppFolder
        for(String overlay in project.gretty.overlays)
          task.dependsOn "$overlay:classes" as String
      }

      def setupWarDependencies = { task ->
        if(project.gretty.overlays)
          task.dependsOn project.tasks.overlayWar
        else
          task.dependsOn project.tasks.war
      }

      def run = { Map options ->
        project.gretty.onStart*.call()
        def json = new JsonBuilder()
        json {
          inplace options.inplace as boolean
          interactive options.interactive as boolean
          port project.gretty.port
          servicePort project.gretty.servicePort
          contextPath ProjectUtils.getContextPath(project)
          resourceBase (options.inplace ? "${project.buildDir}/inplaceWebapp" : ProjectUtils.getFinalWarPath(project).toString())
          initParams ProjectUtils.getInitParameters(project)
          realmInfo ProjectUtils.getRealmInfo(project)
          projectClassPath ProjectUtils.getClassPath(project, options.inplace)
          if(ProjectUtils.findFileInClassPath(project, ~/logback\.(xml|groovy)/))
            project.logger.warn 'logback config file detected, gretty will not configure logback'
          else {
            project.logger.warn 'logback config file is not detected, gretty will configure logback'
            logging {
              loggingLevel project.gretty.loggingLevel
              consoleLogEnabled project.gretty.consoleLogEnabled
              fileLogEnabled project.gretty.fileLogEnabled
              logFileName project.gretty.logFileName ?: project.name
              logDir project.gretty.logDir
            }
          }
        }
        ScannerManagerBase scanman = createScannerManager()
        scanman.startScanner(project, options.inplace)
        try {
          project.javaexec {
            classpath = project.configurations.grettyHelperConfig
            main = 'org.akhikhl.gretty.Runner'
            args = [json.toString()]
            standardInput = System.in
            debug = options.debug as boolean
          }
        } finally {
          scanman.stopScanner()
        }
        project.gretty.onStop*.call()
      }

      project.task('jettyRun', group: 'gretty', description: 'Starts jetty server inplace, in interactive mode (keypress stops the server).') { task ->
        setupInplaceWebAppDependencies task
        task.doLast {
          run inplace: true, interactive: true
        }
      }

      project.tasks.run.dependsOn 'jettyRun'

      project.task('jettyRunDebug', group: 'gretty', description: 'Starts jetty server inplace, in debug and interactive mode (keypress stops the server).') { task ->
        setupInplaceWebAppDependencies task
        task.doLast {
          run inplace: true, interactive: true, debug: true
        }
      }

      project.tasks.debug.dependsOn 'jettyRunDebug'

      project.task('jettyRunWar', group: 'gretty', description: 'Starts jetty server on WAR-file, in interactive mode (keypress stops the server).') { task ->
        setupWarDependencies task
        task.doLast {
          run inplace: false, interactive: true
        }
      }

      project.task('jettyRunWarDebug', group: 'gretty', description: 'Starts jetty server on WAR-file, in debug and interactive mode (keypress stops the server).') { task ->
        setupWarDependencies task
        task.doLast {
          run inplace: false, interactive: true, debug: true
        }
      }

      project.task('jettyStart', group: 'gretty', description: 'Starts jetty server inplace, in batch mode (\'jettyStop\' stops the server).') { task ->
        setupInplaceWebAppDependencies task
        task.doLast {
          run inplace: true, interactive: false
        }
      }

      project.task('jettyStartDebug', group: 'gretty', description: 'Starts jetty server inplace, in debug and batch mode (\'jettyStop\' stops the server).') { task ->
        setupInplaceWebAppDependencies task
        task.doLast {
          run inplace: true, interactive: false, debug: true
        }
      }

      project.task('jettyStartWar', group: 'gretty', description: 'Starts jetty server on WAR-file, in batch mode (\'jettyStop\' stops the server).') { task ->
        setupWarDependencies task
        task.doLast {
          run inplace: false, interactive: false
        }
      }

      project.task('jettyStartWarDebug', group: 'gretty', description: 'Starts jetty server on WAR-file, in debug and batch mode (\'jettyStop\' stops the server).') { task ->
        setupWarDependencies task
        task.doLast {
          run inplace: false, interactive: false, debug: true
        }
      }

      project.task('jettyStop', group: 'gretty', description: 'Sends \'stop\' command to running jetty server.') {
        doLast {
          ServiceControl.send(project.gretty.servicePort, 'stop')
        }
      }

      project.task('jettyRestart', group: 'gretty', description: 'Sends \'restart\' command to running jetty server.') {
        doLast {
          ServiceControl.send(project.gretty.servicePort, 'restart')
        }
      }
    } // afterEvaluate
  } // apply

  abstract ScannerManagerBase createScannerManager()

  abstract void injectDependencies(Project project)

} // GrettyPluginBase
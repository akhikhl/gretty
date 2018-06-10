/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.apache.commons.io.FilenameUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Paths

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class GrettyPlugin implements Plugin<Project> {

  protected static final Logger log = LoggerFactory.getLogger(GrettyPlugin)

  private void addConfigurations(Project project) {
    project.configurations {
      implementation {
        exclude module: 'spring-boot-starter-tomcat'
        exclude module: 'spring-boot-starter-jetty'
      }
      gretty
      grettyStarter
      springBoot {
        exclude module: 'spring-boot-starter-tomcat'
        exclude module: 'spring-boot-starter-jetty'
        exclude group: 'org.eclipse.jetty'
        exclude group: 'org.eclipse.jetty.websocket'
      }
      grettyNoSpringBoot {
        extendsFrom project.configurations.gretty
        exclude group: 'org.springframework.boot'
      }
      grettySpringLoaded {
        transitive = false
      }
      grettyProductRuntime
      grettyProvidedCompile
      project.configurations.findByName('implementation')?.extendsFrom grettyProvidedCompile
    }

    ServletContainerConfig.getConfigs().each { configName, config ->
      project.configurations.create config.servletContainerRunnerConfig
    }
  }

  private void addConfigurationsAfterEvaluate(Project project) {
    def runtimeConfig = project.configurations.findByName('runtimeClasspath')
    project.configurations {
      springBoot {
        if (runtimeConfig)
          extendsFrom runtimeConfig
      }
      grettyProductRuntime {
        if (runtimeConfig)
          extendsFrom runtimeConfig
      }
    }
    // need to configure providedCompile, so that war excludes grettyProvidedCompile artifacts
    def providedCompile = project.configurations.findByName('providedCompile')
    if(providedCompile)
      providedCompile.extendsFrom project.configurations.grettyProvidedCompile
  }

  private void addDependencies(Project project) {

    String grettyVersion = Externalized.getString('grettyVersion')
    String springBootVersion = project.gretty.springBootVersion ?: (project.hasProperty('springBootVersion') ? project.springBootVersion : Externalized.getString('springBootVersion'))
    String springLoadedVersion = project.gretty.springLoadedVersion ?: (project.hasProperty('springLoadedVersion') ? project.springLoadedVersion : Externalized.getString('springLoadedVersion'))
    String springVersion = project.gretty.springVersion ?: (project.hasProperty('springVersion') ? project.springVersion : Externalized.getString('springVersion'))
    String logbackVersion = project.gretty.logbackVersion ?: (project.hasProperty('logbackVersion') ? project.logbackVersion :Externalized.getString('logbackVersion'))

    project.dependencies {
      grettyStarter "org.gretty:gretty-starter:$grettyVersion"
      grettySpringLoaded "org.springframework:springloaded:$springLoadedVersion"
    }

    ServletContainerConfig.getConfig(project.gretty.servletContainer).with { config ->
      def closure = config.servletApiDependencies
      closure = closure.rehydrate(config, closure.owner, closure.thisObject)
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure(project)
    }

    ServletContainerConfig.getConfigs().each { configName, config ->
      def closure = config.servletContainerRunnerDependencies
      closure = closure.rehydrate(config, closure.owner, closure.thisObject)
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure(project)
    }

    if(project.gretty.springBoot) {
      String configName = project.configurations.findByName('implementation') ? 'implementation' : 'springBoot'
      project.dependencies.add configName, "org.springframework.boot:spring-boot-starter-web:$springBootVersion", {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
      }
      project.dependencies.add configName, "org.springframework.boot:spring-boot-starter-websocket:${springBootVersion}", {
        exclude group: 'org.apache.tomcat.embed'
      }
      project.dependencies.add configName, "org.springframework:spring-messaging:$springVersion"
      project.dependencies.add configName, "org.springframework:spring-websocket:$springVersion"
      project.dependencies.add configName, "ch.qos.logback:logback-classic:$logbackVersion"
      configName = project.configurations.findByName('runtimeOnly') ? 'runtimeOnly' : 'springBoot'
      project.dependencies.add configName, "org.gretty:gretty-springboot:$grettyVersion"
    }

    for(String overlay in project.gretty.overlays)
      project.dependencies.add 'grettyProvidedCompile', project.project(overlay)

    def runtimeConfig = project.configurations.findByName('runtimeClasspath')
    if(runtimeConfig) {
      if(runtimeConfig.allDependencies.find { it.name == 'slf4j-api' } && !runtimeConfig.allDependencies.find { it.name in ['slf4j-nop', 'slf4j-simple', 'slf4j-log4j12', 'slf4j-jdk14', 'logback-classic', 'log4j-slf4j-impl'] }) {
        log.warn("Found slf4j-api dependency but no providers were found.  Did you mean to add slf4j-simple? See https://www.slf4j.org/codes.html#noProviders .")
      }
    }

    project.farms.farmsMap.each { fname, farm ->
      farm.webAppRefs.each { wref, options ->
        def typeAndResult = FarmConfigurerUtil.resolveWebAppType(project, options.suppressMavenToProjectResolution, wref)
        def type = typeAndResult[0] as FarmWebappType
        def result = typeAndResult[1]

        def configurationName = ProjectUtils.getFarmConfigurationName(fname)
        if(FarmWebappType.WAR_DEPENDENCY == type) {
          project.configurations.maybeCreate(configurationName)
          project.dependencies.add configurationName, result
        }

        if(type in [FarmWebappType.WAR_DEPENDENCY, FarmWebappType.WAR_FILE]) {
          project.configurations.maybeCreate(configurationName)
          options.dependencies?.each {
            project.dependencies.add configurationName, it
          }
        }
      }
    }
  }

  private void addExtensions(Project project) {

    project.extensions.create('gretty', GrettyExtension)

    project.extensions.create('farm', FarmExtension, project)

    project.extensions.create('farms', FarmsExtension, project)
    project.farms.farmsMap_[''] = project.farm

    project.extensions.create('product', ProductExtension)

    project.extensions.create('products', ProductsExtension)
    project.products.productsMap[''] = project.product
  }

  private void addRepositories(Project project) {
    project.repositories {
      mavenLocal()
      jcenter()
      mavenCentral()
      maven { url 'https://repo.spring.io/release' }
      maven { url 'https://repo.spring.io/milestone' }
      maven { url 'https://repo.spring.io/snapshot' }
    }
  }

  private void addTaskDependencies(Project project) {

    project.tasks.whenObjectAdded { task ->
      if (GradleUtils.instanceOf(task, 'org.akhikhl.gretty.AppStartTask'))
        task.dependsOn {
          // We don't need any task for hard inplace mode.
          task.effectiveInplace ? project.tasks.prepareInplaceWebApp : project.tasks.prepareArchiveWebApp
        }
      else if (GradleUtils.instanceOf(task, 'org.akhikhl.gretty.FarmStartTask')) {
        task.dependsOn {
          task.getWebAppConfigsForProjects().findResults {
            def proj = project.project(it.projectPath)
            boolean inplace = it.inplace == null ? task.inplace : it.inplace
            String prepareTaskName = inplace ? 'prepareInplaceWebApp' : 'prepareArchiveWebApp'
            def projTask = proj.tasks.findByName(prepareTaskName)
            if(!projTask)
              proj.tasks.whenObjectAdded { t ->
                if(t.name == prepareTaskName)
                  task.dependsOn t
              }
            projTask
          }
        }

        def farmName = task.farmName
        project.farms.farmsMap[farmName].webAppRefs.each { wref, options ->
          def typeAndResult = FarmConfigurerUtil.resolveWebAppType(project, options.suppressMavenToProjectResolution, wref)
          def type = typeAndResult[0]
          if(type in [FarmWebappType.WAR_FILE, FarmWebappType.WAR_DEPENDENCY]) {
            if (options.overlays) {
              def warFile
              if (type == FarmWebappType.WAR_FILE) {
                warFile = typeAndResult[1]
              } else if (type == FarmWebappType.WAR_DEPENDENCY) {
                warFile = ProjectUtils.getFileFromConfiguration(project, ProjectUtils.getFarmConfigurationName(farmName), typeAndResult[1])
              }

              def warFileName = warFile.name
              def farmOverlayArchive = project.tasks.findByName("farmOverlayArchive${farmName}${warFileName}")
              task.dependsOn farmOverlayArchive
            }
          }
        }
      }
    }
  }

  private void addTasks(Project project) {

    if(project.tasks.findByName('classes')) { // JVM project?

      project.task('prepareInplaceWebAppFolder', group: 'gretty', type: Copy) {

        description = 'Copies webAppDir of this web-app and all overlays (if any) to ${buildDir}/inplaceWebapp'

        def getInplaceMode = {
          project.tasks.findByName('appRun').effectiveInplaceMode
        }

        // We should track changes in inplaceMode value or plugin would show UP-TO-DATE for this task
        // even if inplaceMode was changed
        inputs.property('inplaceMode', getInplaceMode)

        onlyIf { getInplaceMode() != 'hard' }

        // Attention: call order is important here! Overlay files must be copied prior to this web-app files.

        for (String overlay in project.gretty.overlays) {
          Project overlayProject = project.project(overlay)
          dependsOn { overlayProject.tasks.findByName('prepareInplaceWebAppFolder') }
          from "${overlayProject.buildDir}/inplaceWebapp"
        }

        from ProjectUtils.getWebAppDir(project)

        def closure = project.gretty.webappCopy
        closure = closure.rehydrate(it, closure.owner, closure.thisObject)
        closure()

        into "${project.buildDir}/inplaceWebapp"
      }

      project.task('createInplaceWebAppFolder', group: 'gretty') {

        description = 'Creates directory ${buildDir}/inplaceWebapp'

        dependsOn project.tasks.prepareInplaceWebAppFolder

        File inplaceWebappDir = new File("${project.buildDir}/inplaceWebapp")

        outputs.upToDateWhen {
          inplaceWebappDir.exists()
        }

        doFirst {
          inplaceWebappDir.mkdirs()
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
        dependsOn project.tasks.createInplaceWebAppFolder
        dependsOn project.tasks.prepareInplaceWebAppClasses
      }

      def archiveTask = project.tasks.findByName('war') ?: project.tasks.jar

      archiveTask.configure {
        def closure = project.gretty.webappCopy
        closure = closure.rehydrate(it, closure.owner, closure.thisObject)
        closure()
      }

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

      project.task('appRun', type: AppStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in interactive mode.'
      }

      project.task('appRunDebug', type: AppStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in debug and interactive mode.'
        debug = true
      }

      project.task('appStart', type: AppStartTask, group: 'gretty') {
        description = 'Starts web-app inplace (stopped by \'appStop\').'
        interactive = false
      }

      project.task('appStartDebug', type: AppStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in debug mode (stopped by \'appStop\').'
        interactive = false
        debug = true
      }

      if(project.plugins.findPlugin(org.gradle.api.plugins.WarPlugin)) {

        project.task('appRunWar', type: AppStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file, in interactive mode.'
          inplace = false
        }

        project.task('appRunWarDebug', type: AppStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file, in debug and interactive mode.'
          inplace = false
          debug = true
        }

        project.task('appStartWar', type: AppStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file (stopped by \'appStop\').'
          inplace = false
          interactive = false
        }

        project.task('appStartWarDebug', type: AppStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file, in debug mode (stopped by \'appStop\').'
          inplace = false
          interactive = false
          debug = true
        }
      }

      project.task('appStop', type: AppStopTask, group: 'gretty') {
        description = 'Sends \'stop\' command to a running server.'
      }

      project.task('appRestart', type: AppRestartTask, group: 'gretty') {
        description = 'Sends \'restart\' command to a running server.'
      }

      project.task('appBeforeIntegrationTest', type: AppBeforeIntegrationTestTask, group: 'gretty') {
        description = 'Starts server before integration test.'
      }

      project.task('appAfterIntegrationTest', type: AppAfterIntegrationTestTask, group: 'gretty') {
        description = 'Stops server after integration test.'
      }

      project.task('jettyRun', type: JettyStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in interactive mode.'
      }

      project.task('jettyRunDebug', type: JettyStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in debug and interactive mode.'
        debug = true
      }

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

      project.task('jettyStop', type: AppStopTask, group: 'gretty') {
        description = 'Sends \'stop\' command to a running server.'
      }

      project.task('jettyRestart', type: AppRestartTask, group: 'gretty') {
        description = 'Sends \'restart\' command to a running server.'
      }

      project.task('tomcatRun', type: TomcatStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in interactive mode.'
      }

      project.task('tomcatRunDebug', type: TomcatStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in debug and interactive mode.'
        debug = true
      }

      project.task('tomcatStart', type: TomcatStartTask, group: 'gretty') {
        description = 'Starts web-app inplace (stopped by \'tomcatStop\').'
        interactive = false
      }

      project.task('tomcatStartDebug', type: TomcatStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in debug mode (stopped by \'tomcatStop\').'
        interactive = false
        debug = true
      }

      if(project.plugins.findPlugin(org.gradle.api.plugins.WarPlugin)) {

        project.task('tomcatRunWar', type: TomcatStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file, in interactive mode.'
          inplace = false
        }

        project.task('tomcatRunWarDebug', type: TomcatStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file, in debug and interactive mode.'
          inplace = false
          debug = true
        }

        project.task('tomcatStartWar', type: TomcatStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file (stopped by \'tomcatStop\').'
          inplace = false
          interactive = false
        }

        project.task('tomcatStartWarDebug', type: TomcatStartTask, group: 'gretty') {
          description = 'Starts web-app on WAR-file, in debug mode (stopped by \'tomcatStop\').'
          inplace = false
          interactive = false
          debug = true
        }
      }

      project.task('tomcatStop', type: AppStopTask, group: 'gretty') {
        description = 'Sends \'stop\' command to a running server.'
      }

      project.task('tomcatRestart', type: AppRestartTask, group: 'gretty') {
        description = 'Sends \'restart\' command to a running server.'
      }

      project.ext.jettyBeforeIntegrationTest = project.tasks.ext.jettyBeforeIntegrationTest = project.tasks.appBeforeIntegrationTest

      project.ext.jettyAfterIntegrationTest = project.tasks.ext.jettyAfterIntegrationTest = project.tasks.appAfterIntegrationTest

      project.task('showClassPath', group: 'gretty') {
        description = 'Shows classpath information'
        doLast {
          println "Runner classpath:"
          project.tasks.appRun.runnerClassPath.each { URL url ->
            println " $url"
          }
          project.tasks.appRun.getWebappClassPaths().each { contextPath, cp ->
            println "$contextPath classpath:"
            cp.each { URL url ->
              println " $url"
            }
          }
        }
      }

    } // JVM project

    project.farms.farmsMap.each { fname, farm ->
      def overlayTasks = []
      farm.webAppRefs.each { wref, options ->
        def typeAndResult = FarmConfigurerUtil.resolveWebAppType(project, options.suppressMavenToProjectResolution, wref)
        def type = typeAndResult[0] as FarmWebappType
        switch (type) {
          case FarmWebappType.WAR_FILE:
          case FarmWebappType.WAR_DEPENDENCY:
            def overlays = options.overlays
            if (overlays) {
              log.info("Farm {} contains webapp {} with overlays: {}", fname, wref, options.overlays)

              def warFile
              if(type == FarmWebappType.WAR_FILE) {
                warFile = typeAndResult[1]
              } else if (type == FarmWebappType.WAR_DEPENDENCY) {
                warFile = ProjectUtils.getFileFromConfiguration(project, ProjectUtils.getFarmConfigurationName(fname), typeAndResult[1])
              }

              def outputFolder = Paths.get(project.buildDir.absolutePath, 'farms', fname, 'explodedWebapps', FilenameUtils.removeExtension(warFile.name)).toFile()
              def outputFolderPath = outputFolder.absolutePath

              def explodeWebappTask = project.task("farmExplodeWebapp$fname${warFile.name}", group: 'gretty') {
                description = 'Explode webapp and all overlays into ${buildDir}/farms/${fname}/explodedWebapps/${wref}'
                for(String overlay in overlays) {
                  dependsOn "$overlay:assemble" as  String
                }
                for(String overlay in overlays)
                  inputs.file { ProjectUtils.getFinalArchivePath(project.project(overlay)) }
                inputs.file warFile
                outputs.dir outputFolder
                doLast {
                  ProjectUtils.prepareExplodedFarmWebAppFolder(project, warFile, overlays, outputFolderPath)
                }
              }

              // TODO: gradle way of doing this?
              def repackagedArchive = Paths.get(project.buildDir.absolutePath, 'farms', fname, 'explodedWebapps', warFile.name).toFile().absolutePath
              def archiveWebappTask = project.task("farmOverlayArchive$fname${warFile.name}", group: 'gretty') {
                description = 'Creates archive from exploded web-app in ${buildDir}/farms/${fname}/explodedWebapps/${wref}'
                dependsOn explodeWebappTask
                inputs.dir outputFolder
                outputs.file repackagedArchive
                doLast {
                  ant.zip destfile: repackagedArchive, basedir: outputFolderPath
                }
              }

              overlayTasks.addAll([explodeWebappTask, archiveWebappTask])

              options.finalArchivePath = repackagedArchive
            }
            break
        }
      }

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

      project.task('farmStart' + fname, type: FarmStartTask, group: 'gretty') {
        description = "Starts ${farmDescr} inplace (stopped by 'farmStop${fname}')."
        farmName = fname
        interactive = false
      }

      project.task('farmStartDebug' + fname, type: FarmStartTask, group: 'gretty') {
        description = "Starts ${farmDescr} inplace, in debug mode (stopped by 'farmStop${fname}')."
        farmName = fname
        interactive = false
        debug = true
      }

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

      project.task('farmStartWar' + fname, type: FarmStartTask, group: 'gretty') {
        description = "Starts ${farmDescr} on WAR-files (stopped by 'farmStop${fname}')."
        farmName = fname
        interactive = false
        inplace = false
      }

      project.task('farmStartWarDebug' + fname, type: FarmStartTask, group: 'gretty') {
        description = "Starts ${farmDescr} on WAR-files, in debug (stopped by 'farmStop${fname}')."
        farmName = fname
        interactive = false
        debug = true
        inplace = false
      }

      project.task('farmStop' + fname, type: FarmStopTask, group: 'gretty') {
        description = "Sends \'stop\' command to a running ${farmDescr}."
        farmName = fname
      }

      project.task('farmRestart' + fname, type: FarmRestartTask, group: 'gretty') {
        description = "Sends \'restart\' command to a running ${farmDescr}."
        farmName = fname
      }

      project.task('farmBeforeIntegrationTest' + fname, type: FarmBeforeIntegrationTestTask, group: 'gretty') {
        description = "Starts ${farmDescr} before integration test."
        farmName = fname
      }

      project.task('farmIntegrationTest' + fname, type: FarmIntegrationTestTask, group: 'gretty') {
        description = "Runs integration tests on ${farmDescr} web-apps."
        farmName = fname
        dependsOn 'farmBeforeIntegrationTest' + fname
        finalizedBy 'farmAfterIntegrationTest' + fname
      }

      project.task('farmAfterIntegrationTest' + fname, type: FarmAfterIntegrationTestTask, group: 'gretty') {
        description = "Stops ${farmDescr} after integration test."
        farmName = fname
      }
    } // farmsMap
  } // addTasks

  private void afterProjectEvaluate(Project project) {

    if(project.extensions.findByName('gretty')) {

      addConfigurationsAfterEvaluate(project)
      addTaskDependencies(project)
      new ProductsConfigurer(project).configureProducts()

      if(project.gretty.autoConfigureRepositories)
        addRepositories(project)

      addDependencies(project)
      addTasks(project)

      for(Closure afterEvaluateClosure in project.gretty.afterEvaluate) {
        afterEvaluateClosure.delegate = project.gretty
        afterEvaluateClosure.resolveStrategy = Closure.DELEGATE_FIRST
        afterEvaluateClosure()
      }

      project.tasks.findByName('appBeforeIntegrationTest')?.with {
        if(!integrationTestTaskAssigned)
          integrationTestTask null // default binding
      }

      project.tasks.findByName('appAfterIntegrationTest')?.with {
        if(!integrationTestTaskAssigned)
          integrationTestTask null // default binding
      }

      if(!project.tasks.findByName('run') && project.hasProperty('gretty_runTask') && Boolean.valueOf(project.gretty_runTask))
        project.task('run', group: 'gretty') {
          description = 'Starts web-app inplace, in interactive mode. Same as appRun task.'
          dependsOn 'appRun'
        }

      if(!project.tasks.findByName('debug') && project.hasProperty('gretty_debugTask') && Boolean.valueOf(project.gretty_debugTask))
        project.task('debug', group: 'gretty') {
          description = 'Starts web-app inplace, in debug and interactive mode. Same as appRunDebug task.'
          dependsOn 'appRunDebug'
        }
    }
  }

  private void afterAllProjectsEvaluate(Project rootProject) {

    rootProject.allprojects { project ->

      if(project.extensions.findByName('farms'))
        project.farms.farmsMap.each { fname, farm ->

          for(Closure afterEvaluateClosure in farm.afterEvaluate) {
            afterEvaluateClosure.delegate = farm
            afterEvaluateClosure.resolveStrategy = Closure.DELEGATE_FIRST
            afterEvaluateClosure()
          }

          if(!project.tasks."farmBeforeIntegrationTest$fname".integrationTestTaskAssigned)
            project.tasks."farmBeforeIntegrationTest$fname".integrationTestTask null // default binding

          if(!project.tasks."farmIntegrationTest$fname".integrationTestTaskAssigned)
            project.tasks."farmIntegrationTest$fname".integrationTestTask null // default binding

          if(!project.tasks."farmAfterIntegrationTest$fname".integrationTestTaskAssigned)
            project.tasks."farmAfterIntegrationTest$fname".integrationTestTask null // default binding
        }
    }
  }

  void apply(final Project project) {

    if(project.gradle.gradleVersion.startsWith('1.')) {
      String releaseNumberStr = project.gradle.gradleVersion.split('\\.')[1]
      if(releaseNumberStr.contains('-'))
        releaseNumberStr = releaseNumberStr.split('-')[0]
      int releaseNumber = releaseNumberStr as int
      if(releaseNumber < 10)
        throw new GradleException("Gretty supports only Gradle 1.10 or newer. You have Gradle ${project.gradle.gradleVersion}.")
    }

    project.ext {
      grettyVersion = Externalized.getString('grettyVersion')
      if(!has('jetty7Version'))
        jetty7Version = Externalized.getString('jetty7Version')
      if(!has('jetty7ServletApiVersion'))
        jetty7ServletApiVersion = Externalized.getString('jetty7ServletApiVersion')
      if(!has('jetty8Version'))
        jetty8Version = Externalized.getString('jetty8Version')
      if(!has('jetty8ServletApiVersion'))
        jetty8ServletApiVersion = Externalized.getString('jetty8ServletApiVersion')
      if(!has('jetty9Version'))
        jetty9Version = Externalized.getString('jetty9Version')
      if(!has('jetty93Version'))
        jetty93Version = Externalized.getString('jetty93Version')
      if(!has('jetty94Version'))
        jetty94Version = Externalized.getString('jetty94Version')
      if(!has('jetty9ServletApiVersion'))
        jetty9ServletApiVersion = Externalized.getString('jetty9ServletApiVersion')
      if(!has('tomcat7Version'))
        tomcat7Version = Externalized.getString('tomcat7Version')
      if(!has('tomcat7ServletApiVersion'))
        tomcat7ServletApiVersion = Externalized.getString('tomcat7ServletApiVersion')
      if(!has('tomcat8Version'))
        tomcat8Version = Externalized.getString('tomcat8Version')
      if(!has('tomcat8ServletApiVersion'))
        tomcat8ServletApiVersion = Externalized.getString('tomcat8ServletApiVersion')
      if(!has('tomcat85Version'))
        tomcat85Version = Externalized.getString('tomcat85Version')
      if(!has('tomcat85ServletApiVersion'))
        tomcat85ServletApiVersion = Externalized.getString('tomcat85ServletApiVersion')
      if(!has('tomcat9Version'))
        tomcat9Version = Externalized.getString('tomcat9Version')
      if(!has('tomcat9ServletApiVersion'))
        tomcat9ServletApiVersion = Externalized.getString('tomcat9ServletApiVersion')
      if(!has('asmVersion'))
        asmVersion = Externalized.getString('asmVersion')
    }

    addExtensions(project)
    addConfigurations(project)

    if(!project.rootProject.hasProperty('gretty_')) {
      Project rootProject = project.rootProject
      rootProject.ext.gretty_ = [:]
      rootProject.ext.gretty_.evalProjectCount = rootProject.allprojects.sum 0, { it.state.executed ? 0 : 1 }
      for(def p in rootProject.allprojects)
        p.afterEvaluate { proj ->
          afterProjectEvaluate(proj)
          rootProject.ext.gretty_.evalProjectCount = rootProject.ext.gretty_.evalProjectCount - 1
          if(rootProject.ext.gretty_.evalProjectCount == 0)
            afterAllProjectsEvaluate(rootProject)
        }
    }
  } // apply
}

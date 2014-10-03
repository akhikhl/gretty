/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 *
 * @author akhikhl
 */
class GrettyPlugin implements Plugin<Project> {

  protected static final Logger log = LoggerFactory.getLogger(GrettyPlugin)

  private void addConfigurations(Project project) {
    project.configurations {
      gretty
      springBoot {
        if(project.configurations.findByName('runtime'))
          extendsFrom project.configurations.runtime
        exclude module: 'spring-boot-starter-tomcat'
        exclude group: 'org.eclipse.jetty'
      }
      grettyNoSpringBoot {
        extendsFrom project.configurations.gretty
        exclude group: 'org.springframework.boot'
      }
      grettyRunnerSpringBootBase
      grettyRunnerSpringBoot {
        extendsFrom project.configurations.grettyRunnerSpringBootBase
      }
      grettyRunnerSpringBootJettyBase {
        extendsFrom project.configurations.grettyRunnerSpringBoot
      }
      grettyRunnerSpringBootJetty {
        extendsFrom project.configurations.grettyRunnerSpringBootJettyBase
      }
      grettyRunnerSpringBootTomcatBase {
        extendsFrom project.configurations.grettyRunnerSpringBoot
      }
      grettyRunnerSpringBootTomcat {
        extendsFrom project.configurations.grettyRunnerSpringBootTomcatBase
      }
      grettySpringLoaded {
        transitive = false
      }
      grettyStarter
      runtimeNoSpringBoot {
        extendsFrom project.configurations.runtime
        exclude group: 'org.springframework.boot'
      }
    }
    if(!project.configurations.findByName('providedCompile'))
      project.configurations {
        providedCompile
        compile.extendsFrom(providedCompile)
      }
    ServletContainerConfig.getConfigs().each { configName, config ->
      project.configurations.create config.servletContainerRunnerConfig
    }
    SpringBootResolutionStrategy.apply(project)
  }

  private void addDependencies(Project project) {

    String grettyVersion = Externalized.getString('grettyVersion')    
    String springBootVersion = project.gretty.springBootVersion ?: (project.hasProperty('springBootVersion') ? project.springBootVersion : '1.1.5.RELEASE')

    project.dependencies {
      grettyRunnerSpringBootBase "org.akhikhl.gretty:gretty-runner-spring-boot:$grettyVersion", {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-web'
      }
      grettyRunnerSpringBoot "org.springframework.boot:spring-boot-starter-web:${springBootVersion}", {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
      }
      grettyRunnerSpringBootJettyBase "org.akhikhl.gretty:gretty-runner-spring-boot-jetty:$grettyVersion", {
        // concrete implementation is chosen depending on servletContainer property
        exclude group: 'org.akhikhl.gretty', module: 'gretty-runner-jetty9'
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-jetty'
      }
      grettyRunnerSpringBootJetty "org.springframework.boot:spring-boot-starter-jetty:${springBootVersion}", {
        exclude group: 'org.eclipse.jetty'
        exclude group: 'javax.servlet', module: 'javax.servlet-api'
      }
      grettyRunnerSpringBootTomcatBase "org.akhikhl.gretty:gretty-runner-spring-boot-tomcat:$grettyVersion", {
        // concrete implementation is chosen depending on servletContainer property
        exclude group: 'org.apache.tomcat.embed'
        exclude group: 'javax.servlet', module: 'javax.servlet-api'
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
      }
      grettyRunnerSpringBootTomcat "org.springframework.boot:spring-boot-starter-tomcat:${springBootVersion}", {
        exclude group: 'org.apache.tomcat.embed'
        exclude group: 'javax.servlet', module: 'javax.servlet-api'
      }
      grettySpringLoaded 'org.springframework:springloaded:1.2.0.RELEASE'
      grettyStarter "org.akhikhl.gretty:gretty-starter:$grettyVersion"
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

    if(project.gretty.springBoot)
      project.dependencies {
        compile "org.springframework.boot:spring-boot-starter-web:${springBootVersion}", {
          exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
        }
        springBoot "org.springframework.boot:spring-boot-starter-jetty:${springBootVersion}"
      }

    for(String overlay in project.gretty.overlays)
      project.dependencies.add 'providedCompile', project.project(overlay)
  }

  private void addExtensions(Project project) {

    project.extensions.create('gretty', GrettyExtension)

    project.extensions.create('farm', FarmExtension)

    project.extensions.create('farms', FarmsExtension)
    project.farms.farmsMap[''] = project.farm

    project.extensions.create('product', ProductExtension)

    project.extensions.create('products', ProductsExtension)
    project.products.productsMap[''] = project.product
  }

  private void addRepositories(Project project) {
    project.repositories {
      mavenLocal()
      jcenter()
      mavenCentral()
      maven { url 'http://repo.spring.io/release' }
      maven { url 'http://repo.spring.io/milestone' }
      maven { url 'http://repo.spring.io/snapshot' }
    }
  }

  private void addTaskDependencies(Project project) {

    project.tasks.whenObjectAdded { task ->
      if(task instanceof AppStartTask)
        task.dependsOn {
          // We don't need any task for hard inplace mode.
          task.effectiveInplace ? project.tasks.prepareInplaceWebApp : project.tasks.prepareArchiveWebApp
        }
      else if(task instanceof FarmStartTask)
        task.dependsOn {
          task.getWebAppConfigsForProjects().collect {
            def proj = project.project(it.projectPath)
            boolean inplace = it.inplace == null ? task.inplace : it.inplace
            inplace ? proj.tasks.prepareInplaceWebApp : proj.tasks.prepareArchiveWebApp
          }
        }
    }
  }

  private void addTasks(Project project) {

    if(project.tasks.findByName('classes')) { // JVM project?

      project.task('prepareInplaceWebAppFolder', group: 'gretty') {
        description = 'Copies webAppDir of this web-app and all overlays (if any) to ${buildDir}/inplaceWebapp'
        def getInplaceMode = {
            project.tasks.findByName('appRun').effectiveInplaceMode
        }
        inputs.dir ProjectUtils.getWebAppDir(project)
        // We should track changes in inplaceMode value or plugin would show UP-TO-DATE for this task
        // even if inplaceMode was changed
        inputs.property('inplaceMode', getInplaceMode)
        outputs.dir "${project.buildDir}/inplaceWebapp"
        doLast {
            if(getInplaceMode() != 'hard') {
                // Skipping this task for hard inplaceMode.
                ProjectUtils.prepareInplaceWebAppFolder(project)
            }
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

      archiveTask.configure project.gretty.webappCopy

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

      project.tasks.run.dependsOn 'appRun'

      project.task('appRunDebug', type: AppStartTask, group: 'gretty') {
        description = 'Starts web-app inplace, in debug and interactive mode.'
        debug = true
      }

      project.tasks.debug.dependsOn 'appRunDebug'

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

    } // JVM project

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

  private void afterAfterEvaluate(Project project) {

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

    project.farms.farmsMap.each { fname, farm ->

      for(Closure afterEvaluateClosure in farm.afterEvaluate) {
        afterEvaluateClosure.delegate = farm
        afterEvaluateClosure.resolveStrategy = Closure.DELEGATE_FIRST
        afterEvaluateClosure()
      }

      if(!project.tasks.farmBeforeIntegrationTest.integrationTestTaskAssigned)
        project.tasks.farmBeforeIntegrationTest.integrationTestTask null // default binding

      if(!project.tasks.farmIntegrationTest.integrationTestTaskAssigned)
        project.tasks.farmIntegrationTest.integrationTestTask null // default binding

      if(!project.tasks.farmAfterIntegrationTest.integrationTestTaskAssigned)
        project.tasks.farmAfterIntegrationTest.integrationTestTask null // default binding
    }
  }

  void apply(final Project project) {
  
    if(project.gradle.gradleVersion.startsWith('1.')) {
      int releaseNumber = project.gradle.gradleVersion.split('\\.')[1] as int
      if(releaseNumber < 12)
        throw new GradleException("Gretty supports only Gradle 1.12 or newer. You have Gradle ${project.gradle.gradleVersion}.")
    }

    addExtensions(project)
    addConfigurations(project)

    if(!project.tasks.findByName('run'))
      project.task('run')

    if(!project.tasks.findByName('debug'))
      project.task('debug')

    addTaskDependencies(project)

    new ProductsConfigurer(project).configureProducts()

    project.afterEvaluate {

      addRepositories(project)
      addDependencies(project)
      addTasks(project)
      afterAfterEvaluate(project)

    } // afterEvaluate
  } // apply
}

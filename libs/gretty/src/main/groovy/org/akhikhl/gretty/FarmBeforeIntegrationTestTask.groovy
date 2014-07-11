/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.process.JavaForkOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class FarmBeforeIntegrationTestTask extends FarmStartTask {

  private static final Logger log = LoggerFactory.getLogger(FarmBeforeIntegrationTestTask)

  private String integrationTestTask_
  private boolean integrationTestTaskAssigned

  FarmBeforeIntegrationTestTask() {
    def thisTask = this
    mustRunAfter {
      List projects = project.rootProject.allprojects as List
      def result = []
      int thisProjectIndex = projects.indexOf(project)
      if(thisProjectIndex > 0)
        result.addAll projects[0..thisProjectIndex - 1].findAll { it.extensions.findByName('farms') }.collectMany { proj ->
          proj.extensions.farms.farmsMap.keySet().collect { proj.tasks['farmAfterIntegrationTest' + it] }
        }
      def farms = project.extensions.farms.farmsMap.keySet() as List
      def thisFarmIndex = farms.indexOf(farmName)
      if(thisFarmIndex > 0)
        result.addAll farms[0..thisFarmIndex - 1].collect { project.tasks['farmAfterIntegrationTest' + it] }
      //def collectIntegrationTaskKeys = { it.getWebAppProjects().findResults { it.tasks.findByName(thisTask.integrationTestTask) ? it.path + ':' + thisTask.integrationTestTask : null } }
      result.each { fait ->
        if(thisTask.integrationTestTask == fait.integrationTestTask) {
          def intersection = thisTask.getWebAppProjects().intersect(fait.getWebAppProjects())
          if(intersection)
            throw new GradleException("Could not properly order farm tasks: $thisTask intersects with $fait over projects ${intersection.collect { it.path } }")
        }
      }
      result
    }
    doFirst {
      getWebAppProjects().each { proj ->
        proj.tasks.each { t ->
          if((t instanceof AppBeforeIntegrationTestTask || t instanceof AppAfterIntegrationTestTask) && t.integrationTestTask == thisTask.integrationTestTask)
            t.enabled = false
        }
      }
    }
  }

  @Override
  protected boolean getDefaultJacocoEnabled() {
    true
  }

  @Override
  protected boolean getIntegrationTest() {
    true
  }

  String getIntegrationTestTask() {
    integrationTestTask_ ?: new FarmConfigurer(project).getProjectFarm(farmName).integrationTestTask
  }

  boolean getIntegrationTestTaskAssigned() {
    integrationTestTaskAssigned
  }

  @Override
  protected boolean getManagedClassReload(ServerConfig sconfig) {
    // disable managed class reloads on integration tests
    false
  }

  void integrationTestTask(String integrationTestTask) {
    if(integrationTestTaskAssigned) {
      log.warn '{}.integrationTestTask is already set to "{}", so "{}" is ignored', name, getIntegrationTestTask(), integrationTestTask
      return
    }
    integrationTestTask_ = integrationTestTask
    def thisTask = this
    getWebAppProjects().each { proj ->
      proj.tasks.all { t ->
        if(t.name == thisTask.integrationTestTask) {
          t.mustRunAfter thisTask
          thisTask.mustRunAfter proj.tasks.testClasses
          if(t.name != 'test' && project.tasks.findByName('test'))
            thisTask.mustRunAfter project.tasks.test
          if(t instanceof JavaForkOptions) {
            t.doFirst {
              if(thisTask.didWork)
                passSystemPropertiesToIntegrationTask(proj, t)
            }
          }
        } else if(t instanceof AppBeforeIntegrationTestTask && t.integrationTestTask == thisTask.integrationTestTask)
          t.mustRunAfter thisTask // need this to be able to disable AppBeforeIntegrationTestTask in doFirst
      }
    }
    integrationTestTaskAssigned = true
  }

  protected void passSystemPropertiesToIntegrationTask(Project webappProject, JavaForkOptions task) {
    FarmConfigurer configurer = new FarmConfigurer(project)
    FarmExtension tempFarm = new FarmExtension()
    configurer.configureFarm(tempFarm, new FarmExtension(serverConfig: serverConfig, webAppRefs: webAppRefs), configurer.getProjectFarm(farmName))
    def options = tempFarm.webAppRefs.find { key, value -> configurer.resolveWebAppRefToProject(key) == webappProject }.value
    def webappConfig = configurer.getWebAppConfigForProject(options, webappProject, inplace)
    ProjectUtils.prepareToRun(project, webappConfig)

    def host = tempFarm.serverConfig.host

    def contextPath = webappConfig.contextPath
    task.systemProperty 'gretty.host', host
    task.systemProperty 'gretty.contextPath', contextPath

    String preferredProtocol
    String preferredBaseURI

    def httpPort = tempFarm.serverConfig.httpPort
    String httpBaseURI
    if(httpPort && tempFarm.serverConfig.httpEnabled) {
      task.systemProperty 'gretty.port', httpPort
      task.systemProperty 'gretty.httpPort', httpPort
      httpBaseURI = "http://${host}:${httpPort}${contextPath}"
      task.systemProperty 'gretty.baseURI', httpBaseURI
      task.systemProperty 'gretty.httpBaseURI', httpBaseURI
      preferredProtocol = 'http'
      preferredBaseURI = httpBaseURI
    }

    def httpsPort = tempFarm.serverConfig.httpsPort
    String httpsBaseURI
    if(httpsPort && tempFarm.serverConfig.httpsEnabled) {
      task.systemProperty 'gretty.httpsPort', httpsPort
      httpsBaseURI = "https://${host}:${httpsPort}${contextPath}"
      task.systemProperty 'gretty.httpsBaseURI', httpsBaseURI
      preferredProtocol = 'https'
      preferredBaseURI = httpsBaseURI
    }

    if(preferredProtocol)
      task.systemProperty 'gretty.preferredProtocol', preferredProtocol
    if(preferredBaseURI)
      task.systemProperty 'gretty.preferredBaseURI', preferredBaseURI

    task.systemProperty 'gretty.farm', (farmName ?: 'default')
  }
}

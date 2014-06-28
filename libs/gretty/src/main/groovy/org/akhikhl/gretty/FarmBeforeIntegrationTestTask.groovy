/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

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
    doFirst {
      getWebAppProjects().each {
        it.tasks.each { t ->
          if(t instanceof AppBeforeIntegrationTestTask || t instanceof AppAfterIntegrationTestTask)
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

  void integrationTestTask(String integrationTestTask) {
    if(integrationTestTaskAssigned) {
      log.warn '{}.integrationTestTask is already set to "{}", so "{}" is ignored', name, getIntegrationTestTask(), integrationTestTask
      return
    }
    integrationTestTask_ = integrationTestTask
    def thisTask = this
    getWebAppProjects().each { webappProject ->
      webappProject.tasks.all { t ->
        if(t.name == thisTask.integrationTestTask) {
          t.mustRunAfter thisTask
          thisTask.mustRunAfter webappProject.tasks.testClasses
          if(t.name != 'test' && project.tasks.findByName('test'))
            thisTask.mustRunAfter project.tasks.test
          if(t instanceof JavaForkOptions) {
            t.doFirst {
              if(thisTask.didWork)
                passSystemPropertiesToIntegrationTask(webappProject, t)
            }
          }
        } else if(t instanceof AppBeforeIntegrationTestTask)
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
    webappConfig.prepareToRun()

    def host = tempFarm.serverConfig.host

    def contextPath = webappConfig.contextPath
    task.systemProperty 'gretty.contextPath', contextPath

    def httpPort = tempFarm.serverConfig.httpPort
    if(httpPort && tempFarm.serverConfig.httpEnabled) {
      task.systemProperty 'gretty.port', httpPort
      task.systemProperty 'gretty.httpPort', httpPort
      def baseURI = "http://${host}:${httpPort}${contextPath}"
      task.systemProperty 'gretty.baseURI', baseURI
      task.systemProperty 'gretty.httpBaseURI', baseURI
    }

    def httpsPort = tempFarm.serverConfig.httpsPort
    if(httpsPort && tempFarm.serverConfig.httpsEnabled) {
      task.systemProperty 'gretty.httpsPort', httpsPort
      task.systemProperty 'gretty.httpsBaseURI', "https://${host}:${httpsPort}${contextPath}"
    }

    task.systemProperty 'gretty.farm', (farmName ?: 'default')
  }
}

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
          if(t instanceof JettyBeforeIntegrationTestTask || t instanceof JettyAfterIntegrationTestTask)
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
        } else if(t instanceof JettyBeforeIntegrationTestTask)
          t.mustRunAfter thisTask // need this to be able to disable JettyBeforeIntegrationTestTask in doFirst
      }
    }
    integrationTestTaskAssigned = true
  }

  protected void passSystemPropertiesToIntegrationTask(Project webappProject, JavaForkOptions task) {
    FarmConfigurer configurer = new FarmConfigurer(project)
    Farm tempFarm = new Farm()
    configurer.configureFarm(tempFarm, new Farm(serverConfig: serverConfig, webAppRefs: webAppRefs), configurer.getProjectFarm(farmName))
    def port = tempFarm.serverConfig.port
    def options = tempFarm.webAppRefs.find { key, value -> configurer.resolveWebAppRefToProject(key) == webappProject }.value
    def webappConfig = configurer.getWebAppConfigForProject(options, webappProject, inplace)
    webappConfig.prepareToRun()
    def contextPath = webappConfig.contextPath
    task.systemProperty 'gretty.port', port
    task.systemProperty 'gretty.contextPath', contextPath
    task.systemProperty 'gretty.baseURI', "http://localhost:${port}${contextPath}"
    task.systemProperty 'gretty.farm', (farmName ?: 'default')
  }
}

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.process.JavaForkOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class FarmBeforeIntegrationTestTask extends FarmStartTask {

  String integrationTestTask

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

  String getEffectiveIntegrationTestTask() {
    integrationTestTask ?: new FarmConfigurer(project).getProjectFarm(farmName).integrationTestTask
  }

  @Override
  protected boolean getIntegrationTest() {
    true
  }

  void setupIntegrationTestTaskDependencies() {
    def thisTask = this
    FarmConfigurer configurer
    Farm tempFarm
    getWebAppProjects().each { proj ->
      proj.tasks.all { t ->
        if(t.name == thisTask.effectiveIntegrationTestTask) {
          t.mustRunAfter thisTask
          thisTask.mustRunAfter proj.tasks.testClasses
          if(t.name != 'test' && project.tasks.findByName('test'))
            thisTask.mustRunAfter project.tasks.test
          if(t instanceof JavaForkOptions) {
            t.doFirst {
              if(thisTask.didWork) {
                if(!configurer) {
                  configurer = new FarmConfigurer(thisTask.project)
                  tempFarm = new Farm()
                  configurer.configureFarm(tempFarm, new Farm(serverConfig: thisTask.serverConfig, webAppRefs: thisTask.webAppRefs), configurer.getProjectFarm(farmName))
                }
                def port = tempFarm.serverConfig.port
                t.systemProperty 'gretty.port', port
                def options = tempFarm.webAppRefs.find { key, value -> configurer.resolveWebAppRefToProject(key) == proj }.value
                def webappConfig = configurer.getWebAppConfigForProject(options, proj, thisTask.inplace)
                webappConfig.prepareToRun()
                def contextPath = webappConfig.contextPath
                t.systemProperty 'gretty.contextPath', contextPath
                t.systemProperty 'gretty.baseURI', "http://localhost:${port}${contextPath}"
                t.systemProperty 'gretty.farm', (farmName ?: 'default')
              }
            }
          }
        } else if(t instanceof JettyBeforeIntegrationTestTask)
          t.mustRunAfter thisTask
      }
    }
  }
}

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.process.JavaForkOptions

/**
 *
 * @author akhikhl
 */
class JettyBeforeIntegrationTestTask extends JettyStartTask {

  String integrationTestTask

  @Override
  protected boolean getIntegrationTest() {
    true
  }

  String getEffectiveIntegrationTestTask() {
    integrationTestTask ?: project.gretty.integrationTestTask
  }

  void setupIntegrationTestTaskDependencies() {
    def thisTask = this
    project.tasks.all { t ->
      if(t.name == thisTask.effectiveIntegrationTestTask) {
        t.dependsOn thisTask
        thisTask.dependsOn project.tasks.testClasses
        if(t instanceof JavaForkOptions) {
          t.doFirst {
            if(thisTask.didWork) {
              def runConfig = thisTask.getRunConfig()
              def port = runConfig.serverConfig.port
              def contextPath = runConfig.webAppConfigs[0].contextPath
              systemProperty 'gretty.port', port
              systemProperty 'gretty.contextPath', contextPath
              systemProperty 'gretty.baseURI', "http://localhost:${port}${contextPath}"
            }
          }
        }
      }
    }
  }
}

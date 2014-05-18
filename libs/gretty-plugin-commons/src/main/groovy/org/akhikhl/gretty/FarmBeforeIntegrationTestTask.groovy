/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
class FarmBeforeIntegrationTestTask extends FarmStartTask {

  FarmBeforeIntegrationTestTask() {
    doFirst {
      getWebAppConfigsForProjects().each { webAppConfig ->
        def proj = project.project(it.projectPath)
        proj.tasks.each { t ->
          if(t instanceof JettyBeforeIntegrationTestTask || t instanceof JettyAfterIntegrationTestTask)
            t.enabled = false
        }
      }
    }
  }

  @Override
  protected boolean getIntegrationTest() {
    true
  }

  void setupIntegrationTestTaskDependencies() {
    def thisTask = this
    getWebAppConfigsForProjects().each { webAppConfig ->
      def proj = project.project(it.projectPath)
      proj.tasks.all { t ->
        if(t.name == thisTask.integrationTestTask) {
          t.dependsOn thisTask
          thisTask.dependsOn proj.tasks.testClasses
        } else if(t instanceof JettyBeforeIntegrationTestTask)
          t.mustRunAfter thisTask
      }
    }
  }
}

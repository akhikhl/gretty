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
class JettyBeforeIntegrationTestTask extends JettyStartTask {

  String integrationTestTask

  @Override
  protected boolean getIntegrationTest() {
    true
  }

  String getIntegrationTestTask() {
    integrationTestTask ?: project.gretty.integrationTestTask
  }

  void setupIntegrationTestTaskDependencies() {
    def thisTask = this
    project.tasks.all { t ->
      if(t.name == thisTask.integrationTestTask) {
        t.dependsOn thisTask
        thisTask.dependsOn project.tasks.testClasses
      }
    }
  }
}

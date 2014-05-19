/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.tasks.TaskAction

/**
 *
 * @author akhikhl
 */
class JettyAfterIntegrationTestTask extends JettyStopTask {

  String integrationTestTask

  @TaskAction
  void action() {
    super.action()
    if(project.ext.has('grettyRunnerThread') && project.ext.grettyRunnerThread != null) {
      project.ext.grettyRunnerThread.join()
      project.ext.grettyRunnerThread = null
    }
  }

  String getEffectiveIntegrationTestTask() {
    integrationTestTask ?: project.gretty.integrationTestTask
  }

  void setupIntegrationTestTaskDependencies() {
    def thisTask = this
    project.tasks.all { t ->
      if(t.name == thisTask.effectiveIntegrationTestTask)
        t.finalizedBy thisTask
    }
  }
}

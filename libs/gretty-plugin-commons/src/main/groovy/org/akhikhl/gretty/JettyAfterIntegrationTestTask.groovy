/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class JettyAfterIntegrationTestTask extends JettyStopTask {

  private static final Logger log = LoggerFactory.getLogger(JettyAfterIntegrationTestTask)

  private String integrationTestTask_
  private boolean integrationTestTaskAssigned

  @TaskAction
  void action() {
    super.action()
    if(project.ext.has('grettyRunnerThread') && project.ext.grettyRunnerThread != null) {
      project.ext.grettyRunnerThread.join()
      project.ext.grettyRunnerThread = null
    }
    System.out.println 'Jetty server stopped.'
  }

  String getIntegrationTestTask() {
    integrationTestTask_ ?: project.gretty.integrationTestTask
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
    project.tasks.all { t ->
      if(t.name == thisTask.integrationTestTask)
        t.finalizedBy thisTask
    }
    integrationTestTaskAssigned = true
  }
}

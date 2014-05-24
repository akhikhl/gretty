/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaForkOptions
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
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
      if(t.name == thisTask.integrationTestTask) {
        t.finalizedBy thisTask
        if(t instanceof JavaForkOptions && project.plugins.findPlugin(JacocoPlugin))
          setupJacocoClientCoverageReportTask(t)
      }
    }
    integrationTestTaskAssigned = true
  }

  protected void setupJacocoClientCoverageReportTask(Task integrationTestTask) {
    String reportTaskName = integrationTestTask.name + 'ClientCoverageReport'
    if(!project.tasks.findByName(reportTaskName)) {
      def reportDir = project.reporting.file("jacoco/$reportTaskName/client/html")
      project.task(reportTaskName, type: JacocoReport) { reportTask ->
        executionData integrationTestTask
        sourceDirectories = project.sourceSets.main.allSource
        classDirectories = project.sourceSets.main.output
        project.sourceSets.each { sourceSet ->
          def srcDirs = project.files(sourceSet.allSource.srcDirs)
          reportTask.sourceDirectories = reportTask.sourceDirectories == null ? srcDirs : reportTask.sourceDirectories + srcDirs
          reportTask.classDirectories = reportTask.classDirectories == null ? sourceSet.output : reportTask.sourceDirectories + sourceSet.output
        }
        reports {
          html.destination = reportDir
        }
        doLast {
          System.out.println "Jacoco report for client created: file://${reportDir.toURI().path}"
        }
      }
      this.finalizedBy project.tasks[reportTaskName]
    }
  }
}

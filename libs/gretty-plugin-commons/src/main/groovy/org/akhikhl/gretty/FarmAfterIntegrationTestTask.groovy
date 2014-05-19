/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 *
 * @author akhikhl
 */
class FarmAfterIntegrationTestTask extends FarmStopTask {

  String integrationTestTask

  protected Map webAppRefs = [:]

  @TaskAction
  void action() {
    super.action()
    if(project.ext.has('grettyRunnerThread') && project.ext.grettyRunnerThread != null) {
      project.ext.grettyRunnerThread.join()
      project.ext.grettyRunnerThread = null
    }
  }

  String getEffectiveIntegrationTestTask() {
    integrationTestTask ?: new FarmConfigurer(project).getProjectFarm(farmName).integrationTestTask
  }

  Iterable<Project> getWebAppProjects() {
    FarmConfigurer configurer = new FarmConfigurer(project)
    Map wrefs = [:]
    FarmConfigurer.mergeWebAppRefMaps(wrefs, webAppRefs)
    FarmConfigurer.mergeWebAppRefMaps(wrefs, configurer.getProjectFarm(farmName).webAppRefs)
    if(!wrefs)
      wrefs = configurer.getDefaultWebAppRefMap()
    configurer.getWebAppProjects(wrefs)
  }

  void setupIntegrationTestTaskDependencies() {
    def thisTask = this
    getWebAppProjects().each {
      it.tasks.all { t ->
        if(t.name == thisTask.effectiveIntegrationTestTask)
          thisTask.mustRunAfter t
        else if(t instanceof JettyAfterIntegrationTestTask)
          thisTask.mustRunAfter t
      }
    }
  }

  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webAppRefs[w] = options
  }
}

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.DefaultTask
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class FarmIntegrationTestTask extends DefaultTask {

  String farmName = ''

  String integrationTestTask

  protected Map webAppRefs = [:]

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
          thisTask.dependsOn t
      }
    }
  }

  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webAppRefs[w] = options
  }
}


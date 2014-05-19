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

  Iterable<WebAppConfig> getWebAppConfigsForProjects() {
    FarmConfigurer configurer = new FarmConfigurer(project)
    Map wrefs = [:]
    FarmConfigurer.mergeWebAppRefMaps(wrefs, webAppRefs)
    FarmConfigurer.mergeWebAppRefMaps(wrefs, configurer.getProjectFarm(farmName).webAppRefs)
    println "DBG 1 wrefs=$wrefs"
    if(!wrefs) {
      wrefs = configurer.getDefaultWebAppRefMap()
      println "DBG 2 wrefs=$wrefs"
    }
    configurer.getWebAppConfigsForProjects(wrefs)
  }

  void setupIntegrationTestTaskDependencies() {
    def thisTask = this
    println "DBG ${thisTask.name}: effectiveIntegrationTestTask=${thisTask.effectiveIntegrationTestTask}"
    println "DBG ${thisTask.name}: webAppProjects=${getWebAppConfigsForProjects().collect { project.project(it.projectPath) }}"
    getWebAppConfigsForProjects().each {
      project.project(it.projectPath).tasks.all { t ->
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


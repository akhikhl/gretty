/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.concurrent.ExecutorService
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

/**
 * Base task for starting jetty
 *
 * @author akhikhl
 */
abstract class StartBaseTask extends DefaultTask {

  boolean interactive = true
  boolean debug = false

  private JacocoHelper jacocoHelper

  @TaskAction
  void action() {
    LauncherConfig config = getLauncherConfig()
    Launcher launcher = anyWebAppUsesSpringBoot(config.getWebAppConfigs()) ? new SpringBootLauncher(project, config) : new DefaultLauncher(project, config)
    launcher.launch()
  }
  
  protected final boolean anyWebAppUsesSpringBoot(Iterable<WebAppConfig> wconfigs) {
    wconfigs.find { wconfig -> 
      wconfig.springBoot || (wconfig.projectPath && ProjectUtils.isSpringBootApp(project.project(wconfig.projectPath)))
    }
  }

  protected boolean getDefaultJacocoEnabled() {
    false
  }

  protected boolean getIntegrationTest() {
    false
  }

  JacocoTaskExtension getJacoco() {
    if(jacocoHelper == null && project.extensions.findByName('jacoco')) {
      jacocoHelper = new JacocoHelper(this)
      jacocoHelper.jacoco.enabled = getDefaultJacocoEnabled()
    }
    jacocoHelper?.jacoco
  }

  abstract LauncherConfig getLauncherConfig()

  protected abstract String getStopTaskName()

  void jacoco(Closure configureClosure) {
    getJacoco()?.with configureClosure
  }
}

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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
    def runner = new Runner(this)
    runner.run()
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

  abstract RunConfig getRunConfig()

  protected abstract String getStopTaskName()

  void jacoco(Closure configureClosure) {
    getJacoco()?.with configureClosure
  }
}

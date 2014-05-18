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
  String stopTask = 'jettyStop'

  @TaskAction
  void action() {
    def runner = new Runner(project, getRunConfig(), interactive, debug, getIntegrationTest(), stopTask)
    runner.run()
  }

  protected boolean getIntegrationTest() {
    false
  }

  protected abstract RunConfig getRunConfig()
}

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.concurrent.ExecutorService
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Base task for starting jetty
 *
 * @author akhikhl
 */
abstract class GrettyStartBaseTask extends GrettyBaseTask {

  protected static final Logger log = LoggerFactory.getLogger(GrettyStartBaseTask)

  boolean interactive = true
  boolean debug = false
  boolean integrationTest = false

  @Override
  void action() {
    ServerConfig sconfig = getServerConfig()
    def javaexec = { Closure closure -> project.javaexec closure } as IJavaExec
    def runner = new Runner(getServerConfig(), getWebApps(), interactive, debug, integrationTest, project.configurations.gretty, project.scannerManagerFactory, project.executorService, javaexec)
    runner.run()
  }

  protected abstract ServerConfig getServerConfig()

  protected abstract List<WebAppRunConfig> getWebApps()
}

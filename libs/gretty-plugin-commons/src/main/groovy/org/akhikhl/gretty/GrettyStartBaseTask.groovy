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

  boolean interactive = true
  boolean debug = false
  boolean integrationTest = false

  @Override
  void action() {
    ServerConfig sconfig = getServerConfig()
    def runner = new Runner(project, getServerConfig(), getWebApps(), interactive, debug, integrationTest)
    runner.run()
  }

  protected abstract ServerConfig getServerConfig()

  protected abstract List<WebAppConfig> getWebApps()
}

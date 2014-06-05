/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import org.akhikhl.gretty.StartBaseTask
import org.akhikhl.gretty.RunConfig
import org.akhikhl.gretty.DefaultLauncher
import org.akhikhl.gretty.ServerConfig
import org.akhikhl.gretty.WebAppConfig
import java.util.concurrent.Executors
import groovy.json.JsonBuilder
import java.util.concurrent.ExecutorService
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class SpringBootLauncher extends DefaultLauncher {

  @Override
  protected String getRunnerClassName() {
    'org.akhikhl.gretty.springboot.Runner'
  }
}

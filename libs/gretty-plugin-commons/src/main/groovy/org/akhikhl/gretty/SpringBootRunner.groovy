/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class SpringBootRunner {
	
  protected static final Logger log = LoggerFactory.getLogger(SpringBootRunner)

  protected final StartBaseTask startTask
  protected final Project project
  protected ServerConfig sconfig
  protected Iterable<WebAppConfig> webAppConfigs
  protected final ExecutorService executorService

  SpringBootRunner(StartBaseTask startTask) {
    this.startTask = startTask
    project = startTask.project
    RunConfig runConfig = startTask.getRunConfig()
    sconfig = runConfig.getServerConfig()
    webAppConfigs = runConfig.getWebAppConfigs()
    executorService = Executors.newSingleThreadExecutor()
  }

  void run() {
    println "running spring-boot app!"
    runSpringBoot()
  }

  protected void runSpringBoot() {
    project.javaexec { spec ->
      spec.classpath = project.configurations.gretty
      spec.main = 'org.akhikhl.gretty.SpringBootRunner'
      spec.args = [ cmdLineJson ]
      spec.debug = startTask.debug
      log.debug 'server-config jvmArgs: {}', sconfig.jvmArgs
      spec.jvmArgs sconfig.jvmArgs
      if(startTask.jacoco) {
        String jarg = startTask.jacoco.getAsJvmArg()
        log.debug 'jacoco jvmArgs: {}', jarg
        spec.jvmArgs jarg
      }
    }
  }  
}

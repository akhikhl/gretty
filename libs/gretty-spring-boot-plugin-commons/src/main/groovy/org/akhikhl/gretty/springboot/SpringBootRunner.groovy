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
import org.akhikhl.gretty.Runner
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
class SpringBootRunner implements Runner {

  protected static final Logger log = LoggerFactory.getLogger(SpringBootRunner)

  protected StartBaseTask startTask
  protected Project project
  protected ServerConfig sconfig
  protected Iterable<WebAppConfig> webAppConfigs
  protected final ExecutorService executorService

  SpringBootRunner() {
    executorService = Executors.newSingleThreadExecutor()
  }

  private getCommandLineJson() {
    def json = new JsonBuilder()
    json {
      mainClass SpringBootMainClassFinder.findMainClass(project)
      servicePort sconfig.servicePort
      statusPort sconfig.statusPort
    }
    json
  }

  private void init(StartBaseTask startTask) {
    this.startTask = startTask
    project = startTask.project
    RunConfig runConfig = startTask.getRunConfig()
    sconfig = runConfig.getServerConfig()
    webAppConfigs = runConfig.getWebAppConfigs()
  }

  @Override
  void run(StartBaseTask startTask) {
    init(startTask)
    runSpringBoot()
  }

  private void runSpringBoot() {

    def cmdLineJson = getCommandLineJson()
    log.warn 'Command-line json: {}', cmdLineJson.toPrettyString()
    cmdLineJson = cmdLineJson.toString()

    // we are going to pass json as argument to java process.
    // under windows we must escape double quotes in process parameters.
    if(System.getProperty("os.name") =~ /(?i).*windows.*/)
      cmdLineJson = cmdLineJson.replace('"', '\\"')

    project.javaexec { spec ->
      spec.classpath = project.files(project.configurations.gretty.files + project.configurations.springBoot.files + [ project.sourceSets.main.output.classesDir, project.sourceSets.main.output.resourcesDir ])
      spec.main = 'org.akhikhl.gretty.springboot.Runner'
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

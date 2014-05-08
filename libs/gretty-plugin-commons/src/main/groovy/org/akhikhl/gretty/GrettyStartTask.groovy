/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask

/**
 * Gradle task for starting jetty
 *
 * @author akhikhl
 */
class GrettyStartTask extends DefaultTask {

  boolean autoStart = true
  boolean inplace = true
  boolean interactive = true
  boolean debug = false
  boolean integrationTest = false
  def logbackConfigFile

  private effectiveLogbackConfigFile

  GrettyStartTask() {
    doFirst() {
      autoConfigure()
    }
    doLast {
      action()
    }
  }

  void action() {
    // synchronously runs jetty with the desired parameters.
    // can be implemented differently in descendant classes.
    run()
  }

  void autoConfigure() {
    effectiveLogbackConfigFile = logbackConfigFile ?: project.gretty.logbackConfigFile
  }

  private File discoverLogbackConfigFile() {
    File result
    if(effectiveLogbackConfigFile) {
      if(effectiveLogbackConfigFile instanceof String)
        result = new File(effectiveLogbackConfigFile)
      if(!result || !result.isAbsolute()) {
        result = new File(project.projectDir, effectiveLogbackConfigFile)
        if(!result || !result.exists())
          result = ProjectUtils.findFileInOutput(project, effectiveLogbackConfigFile)
      }
      if(!result || !result.exists())
        project.logger.warn 'The specified logback config file "{}" does not exist, ignoring', effectiveLogbackConfigFile
      else
        project.logger.warn 'Using specified logback config file "{}"', effectiveLogbackConfigFile
    } else {
      result = ProjectUtils.findFileInOutput(project, ~/logback\.(xml|groovy)/)
      if(result)
        project.logger.warn 'Using discovered logback config file "{}"', result
      else
        project.logger.warn 'Auto-configuring logback'
    }
    return result
  }

  private String prepareJson() {
    def json = new JsonBuilder()
    json {
      projectName project.name
      autoStart autoStart
      inplace inplace
      interactive interactive
      integrationTest integrationTest
      port project.gretty.port
      servicePort project.gretty.servicePort
      integrationTestStatusPort project.gretty.integrationTestStatusPort
      contextPath ProjectUtils.getContextPath(project)
      resourceBase (inplace ? "${project.buildDir}/inplaceWebapp" : ProjectUtils.getFinalWarPath(project).toString())
      initParams ProjectUtils.getInitParameters(project)
      realmInfo ProjectUtils.getRealmInfo(project)
      jettyXml ProjectUtils.getJettyXml(project)
      jettyEnvXml ProjectUtils.getJettyEnvXml(project)
      projectClassPath ProjectUtils.getClassPath(project, inplace)
      if(logbackConfigFile)
        logbackConfig logbackConfigFile.absolutePath
      else
        logging {
          loggingLevel project.gretty.loggingLevel
          consoleLogEnabled project.gretty.consoleLogEnabled
          fileLogEnabled project.gretty.fileLogEnabled
          logFileName project.gretty.logFileName ?: project.name
          logDir project.gretty.logDir
        }
    }
    return json.toString()
  }

  protected void run() {

    project.gretty.onStart*.call()

    File logbackConfigFile = discoverLogbackConfigFile()

    String json = prepareJson()
    project.logger.info json

    // we are going to pass json as argument to java process.
    // under windows we must escape double quotes in process parameters.
    if(System.getProperty("os.name") =~ /(?i).*windows.*/)
      json = json.replace('"', '\\"')

    ScannerManagerBase scanman = createScannerManager()
    scanman.startScanner(project, inplace)
    try {
      project.javaexec { spec ->
        spec.classpath = project.configurations.gretty
        spec.main = 'org.akhikhl.gretty.Runner'
        spec.args = [json]
        spec.jvmArgs = project.gretty.jvmArgs
        spec.standardInput = System.in
        spec.debug = debug
      }
    } finally {
      scanman.stopScanner()
    }

    project.gretty.onStop*.call()
  }
}

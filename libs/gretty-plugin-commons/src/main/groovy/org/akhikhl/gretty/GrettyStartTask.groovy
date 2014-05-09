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
class GrettyStartTask extends GrettyBaseTask {

  boolean autoStart = true
  boolean inplace = true
  boolean interactive = true
  boolean debug = false
  boolean integrationTest = false
  int port
  int servicePort
  String contextPath
  String inplaceResourceBase
  String warResourceBase
  Map initParameters
  ProjectUtils.RealmInfo realmInfo
  String jettyXml
  String jettyEnvXml
  Collection<URL> classPath
  List<String> jvmArgs
  String logbackConfigFile
  String loggingLevel
  Boolean consoleLogEnabled
  Boolean fileLogEnabled
  String logFileName
  String logDir
  int integrationTestStatusPort

  @Override
  void action() {
    // Synchronously runs jetty with the desired parameters.
    // Can be changed in descendant classes.
    run()
  }

  private File discoverLogbackConfigFile() {
    File result
    if(logbackConfigFile) {
      result = new File(logbackConfigFile)
      if(!result.isAbsolute()) {
        result = new File(project.projectDir, logbackConfigFile)
        if(!result.exists())
          result = ProjectUtils.findFileInOutput(project, logbackConfigFile)
      }
      if(!result || !result.exists())
        project.logger.warn 'The specified logback config file "{}" does not exist, ignoring', logbackConfigFile
      else
        project.logger.warn 'Using specified logback config file "{}"', logbackConfigFile
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
    File logbackConfigFile_ = discoverLogbackConfigFile()
    def json = new JsonBuilder()
    json {
      projectName project.name
      autoStart autoStart
      inplace inplace
      interactive interactive
      integrationTest integrationTest
      port port
      servicePort servicePort
      integrationTestStatusPort integrationTestStatusPort
      contextPath contextPath
      resourceBase (inplace ? inplaceResourceBase : warResourceBase)
      initParams initParameters
      realmInfo realmInfo
      jettyXml jettyXml
      jettyEnvXml jettyEnvXml
      projectClassPath classPath
      if(logbackConfigFile_)
        logbackConfig logbackConfigFile_.absolutePath
      else
        logging {
          loggingLevel loggingLevel
          consoleLogEnabled consoleLogEnabled
          fileLogEnabled fileLogEnabled
          logFileName logFileName ?: project.name
          logDir logDir
        }
    }
    return json.toString()
  }

  protected void run() {

    project.gretty.onStart*.call()

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
        spec.jvmArgs = jvmArgs
        spec.standardInput = System.in
        spec.debug = debug
      }
    } finally {
      scanman.stopScanner()
    }

    project.gretty.onStop*.call()
  }

  @Override
  void setupProperties() {
    port = port ?: project.gretty.port
    servicePort = servicePort ?: project.gretty.servicePort
    contextPath = contextPath ?: ProjectUtils.getContextPath(project)
    inplaceResourceBase = inplaceResourceBase ?: "${project.buildDir}/inplaceWebapp"
    warResourceBase = warResourceBase ?: ProjectUtils.getFinalWarPath(project).toString()
    initParameters = initParameters ?: ProjectUtils.getInitParameters(project)
    realmInfo = realmInfo ?: ProjectUtils.getRealmInfo(project)
    jettyXml = jettyXml ?: ProjectUtils.getJettyXml(project)
    jettyEnvXml = jettyEnvXml ?: ProjectUtils.getJettyEnvXml(project)
    classPath = classPath ?: ProjectUtils.getClassPath(project, inplace)
    jvmArgs = jvmArgs ?: project.gretty.jvmArgs
    logbackConfigFile = logbackConfigFile ?: project.gretty.logbackConfigFile
    loggingLevel = loggingLevel ?: project.gretty.loggingLevel
    consoleLogEnabled = consoleLogEnabled == null ?: project.gretty.consoleLogEnabled
    fileLogEnabled = fileLogEnabled == null ?: project.gretty.fileLogEnabled
    logFileName = logFileName ?: project.gretty.logFileName
    logDir = logDir ?: project.gretty.logDir
    integrationTestStatusPort = integrationTestStatusPort ?: project.gretty.integrationTestStatusPort
  }
}

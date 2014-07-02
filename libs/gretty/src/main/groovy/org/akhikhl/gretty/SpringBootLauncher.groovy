/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.text.GStringTemplateEngine
import org.akhikhl.gretty.DefaultLauncher
import org.akhikhl.gretty.WebAppConfig
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.process.JavaExecSpec
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class SpringBootLauncher extends DefaultLauncher {

  protected static final Logger log = LoggerFactory.getLogger(SpringBootLauncher)

  SpringBootLauncher(Project project, LauncherConfig config) {
    super(project, config)
  }

  @Override
  protected Collection<URL> getRunnerClassPath() {
    def servletContainerConfig = getServletContainerConfig()
    def files = project.configurations.grettyNoSpringBoot.files +
      project.configurations[servletContainerConfig.servletContainerRunnerConfig].files
    if(servletContainerConfig.servletContainerType == 'jetty')
      files += project.configurations.grettyRunnerSpringBootJetty.files
    else if(servletContainerConfig.servletContainerType == 'tomcat')
      files += project.configurations.grettyRunnerSpringBootTomcat.files
    for(def wconfig in webAppConfigs) {
      if(wconfig.projectPath) {
        def proj = project.project(wconfig.projectPath)
        if(ProjectUtils.isSpringBootApp(proj))
          files += resolveWebAppClassPath(wconfig)
      }
    }
    files.collect { it.toURL() }
  }

  @Override
  protected String getServerManagerFactory() {
    'org.akhikhl.gretty.SpringBootServerManagerFactory'
  }

  @Override
  protected void prepareToRun(WebAppConfig wconfig) {
    super.prepareToRun(wconfig)
    Set springBootSources = new LinkedHashSet()
    if(wconfig.springBootSources) {
      if(wconfig.springBootSources instanceof Collection)
        springBootSources += wconfig.springBootSources
      else
        springBootSources += wconfig.springBootSources.toString().split(',').collect { it.trim() }
    }
    if(wconfig.projectPath && wconfig.projectPath != project.path) {
      String mainClass = SpringBootMainClassFinder.findMainClass(project.project(wconfig.projectPath))
      if(mainClass && mainClass.contains('.'))
        springBootSources += mainClass.substring(0, mainClass.lastIndexOf('.'))
    }
    wconfig.springBootSources = springBootSources.join(',')
  }

  @Override
  protected void writeLoggingConfig(json) {
    File logbackConfigFile
    if(sconfig.logbackConfigFile)
      logbackConfigFile = sconfig.logbackConfigFile
    else {
      logbackConfigFile = new File(project.buildDir, 'logging/logback.groovy')
      LogbackUtils.generateLogbackConfig(logbackConfigFile, sconfig)
    }
    json.with {
      logbackConfig logbackConfigFile.absolutePath
    }
  }

  @Override
  protected void writeRunConfigJson(json) {
    super.writeRunConfigJson(json)
    json.with {
      springBootMainClass SpringBootMainClassFinder.findMainClass(project)
    }
  }

  protected void writeWebAppClassPath(json, WebAppConfig webAppConfig) {
    if(webAppConfig.projectPath) {
      def proj = project.project(webAppConfig.projectPath)
      if(ProjectUtils.isSpringBootApp(proj)) {
        json.springBoot true
        return // webapp classpath is passed directly to the runner
      }
    }
    super.writeWebAppClassPath(json, webAppConfig)
  }
}

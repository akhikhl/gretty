/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.text.GStringTemplateEngine
import org.gradle.api.Project
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
    def classPath = files.collect { it.toURL() } as LinkedHashSet
    def classPathResolver = config.getWebAppClassPathResolver()
    for(def wconfig in webAppConfigs)
      if(wconfig.projectPath && ProjectUtils.isSpringBootApp(project, wconfig) && classPathResolver) {
        def cp = classPathResolver.resolveWebAppClassPath(wconfig)
        if(cp)
          classPath += cp
      }
    classPath
  }

  @Override
  protected String getServerManagerFactory() {
    'org.akhikhl.gretty.SpringBootServerManagerFactory'
  }
  
  protected String getSpringBootMainClass() {
    sconfig.springBootMainClass ?: 
    SpringBootMainClassFinder.findMainClass(project) ?: 
    webAppConfigs.findResult { it.projectPath ? SpringBootMainClassFinder.findMainClass(project.project(it.projectPath)) : null }
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
      springBootMainClass getSpringBootMainClass()
    }
  }

  protected void writeWebAppClassPath(json, WebAppConfig webAppConfig) {
    if(ProjectUtils.isSpringBootApp(project, webAppConfig)) {
      json.springBoot true
      return // webapp classpath is passed directly to the runner
    }
    super.writeWebAppClassPath(json, webAppConfig)
  }
}

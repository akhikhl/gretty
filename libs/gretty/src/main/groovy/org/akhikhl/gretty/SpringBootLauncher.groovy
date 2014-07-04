/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
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
    if(ProjectUtils.isSpringBootApp(project, webAppConfig)) {
      json.springBoot true
      return // webapp classpath is passed directly to the runner
    }
    super.writeWebAppClassPath(json, webAppConfig)
  }
}

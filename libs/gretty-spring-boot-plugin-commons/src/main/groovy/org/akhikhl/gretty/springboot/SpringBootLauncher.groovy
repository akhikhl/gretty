/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

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

	private static final String SPRING_LOADED_AGENT_CLASSNAME = 'org.springsource.loaded.agent.SpringLoadedAgent'

  protected static final Logger log = LoggerFactory.getLogger(SpringBootLauncher)

  @Override
  protected void configureJavaExec(JavaExecSpec spec) {
    super.configureJavaExec(spec)
    File loadedAgent = getLoadedAgent()
    if(loadedAgent)
      spec.jvmArgs '-javaagent:' + loadedAgent.absolutePath, '-noverify'
  }

  private File getLoadedAgent() {
    if (project.hasProperty('run.agent'))
			return project.file(project.property('run.agent'))
		try {
			Class loadedAgentClass = Class.forName(SPRING_LOADED_AGENT_CLASSNAME, true, this.getClass().classLoader)
			if (loadedAgentClass) {
				def source = loadedAgentClass.getProtectionDomain().getCodeSource()
				if (source)
					return new File(source.getLocation().getFile())
			}
		}
		catch (ClassNotFoundException ex) {
			// ignore
      log.debug 'Class not found: {}', SPRING_LOADED_AGENT_CLASSNAME
		}
		return null
  }

  @Override
  protected String getRunnerClassName() {
    'org.akhikhl.gretty.springboot.Runner'
  }

  @Override
  protected FileCollection getRunnerClassPath() {
    def files = project.configurations.grettyNoSpringBoot.files
    for(def wconfig in webAppConfigs) {
      if(wconfig.projectPath) {
        def proj = project.project(wconfig.projectPath)
        if(proj.configurations.findByName('springBoot'))
          files += resolveWebAppClassPath(wconfig)
      }
    }
    project.files(files)
  }

  @Override
  protected String getRunnerRuntimeConfig(Project proj) {
    proj.configurations.findByName('springBoot') ? 'springBoot' : 'runtime'
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

  protected void writeLoggingConfig(json) {
    File logbackConfigFile
    if(sconfig.logbackConfigFile)
      logbackConfigFile = sconfig.logbackConfigFile
    else {
      logbackConfigFile = new File(project.buildDir, 'logging/logback.groovy')
      logbackConfigFile.parentFile.mkdirs()
      def binding = [
        loggingLevel: sconfig.loggingLevel,
        consoleLogEnabled: sconfig.consoleLogEnabled,
        fileLogEnabled: sconfig.fileLogEnabled,
        logFileName: sconfig.logFileName,
        logDir: sconfig.logDir
      ]
      def template
      getClass().getResourceAsStream('logback-groovy.template').withReader {
        template = new GStringTemplateEngine().createTemplate(it).make(binding)
      }
      logbackConfigFile.text = template.toString()
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
      if(proj.configurations.findByName('springBoot'))
        return // webapp classpath is passed directly to the runner
    }
    super.writeWebAppClassPath(json, webAppConfig)
  }
}

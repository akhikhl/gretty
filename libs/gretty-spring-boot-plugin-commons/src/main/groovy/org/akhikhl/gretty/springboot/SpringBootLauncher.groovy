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
import org.gradle.api.file.FileCollection

/**
 *
 * @author akhikhl
 */
class SpringBootLauncher extends DefaultLauncher {

  @Override
  protected String getRunnerClassName() {
    'org.akhikhl.gretty.springboot.Runner'
  }
  
  @Override
  protected FileCollection getRunnerClassPath() {
    def files = project.configurations.grettyNoSpringBoot.files
    for(def wconfig in webAppConfigs)
      files += resolveWebAppClassPath(wconfig)
    project.files(files)
  }
  
  @Override
  protected String getRunnerRuntimeConfig() {
    'springBoot'
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
    // webapp classpath is passed to the runner
  }
}

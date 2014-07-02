/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.text.GStringTemplateEngine

/**
 *
 * @author akhikhl
 */
class LogbackUtils {
  
  static String generateLogbackConfig(ServerConfig sconfig) {
    def binding = [
      loggingLevel: sconfig.loggingLevel,
      consoleLogEnabled: sconfig.consoleLogEnabled,
      fileLogEnabled: sconfig.fileLogEnabled,
      logFileName: sconfig.logFileName,
      logDir: sconfig.logDir
    ]
    def template
    LogbackUtils.getResourceAsStream('logback-groovy.template').withReader {
      template = new GStringTemplateEngine().createTemplate(it).make(binding)
    }
    template.toString()
  }
	
  static void generateLogbackConfig(File logbackConfigFile, ServerConfig sconfig) {
    logbackConfigFile.parentFile.mkdirs()
    logbackConfigFile.text = generateLogbackConfig(sconfig)
  }
}


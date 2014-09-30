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
      logDir: (sconfig.logDir ?: '${System.getProperty(\'user.home\')}/logs')
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


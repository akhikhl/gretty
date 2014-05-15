/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class ServerRunConfig extends ServerConfig {

  private static File resolveJettyXmlFile(Project project, jettyXmlFile) {
    def file = ProjectUtils.resolveSingleFile(project, jettyXmlFile)
    if(file == null) {
      file = jettyXmlFile
      if(!(file instanceof File))
        file = new File(file)
      if(!file.isAbsolute()) {
        String jettyHome = System.getenv('JETTY_HOME')
        if(!jettyHome)
          jettyHome = System.getProperty('jetty.home')
        if(jettyHome != null) {
          File f = new File(new File(jettyHome, 'etc'), file.path)
          if(f.exists())
            file = f
        }
      }
    }
    file
  }

  protected void setupProperties(Project project, ServerConfig sourceConfig) {
    if(jvmArgs == null) jvmArgs = sourceConfig.jvmArgs
    if(port == null) port = sourceConfig.port ?: 8080
    if(servicePort == null) servicePort = sourceConfig.servicePort ?: 9900
    if(statusPort == null) statusPort = sourceConfig.statusPort ?: 9901
    if(jettyXmlFile == null) jettyXmlFile = sourceConfig.jettyXmlFile ?: 'jetty.xml'
    jettyXmlFile = resolveJettyXmlFile(project, jettyXmlFile)
    if(scanInterval == null) scanInterval = sourceConfig.scanInterval
    if(logbackConfigFile == null) logbackConfigFile = sourceConfig.logbackConfigFile
    def f = ProjectUtils.resolveSingleFile(project, logbackConfigFile)
    if(f == null && logbackConfigFile != 'logback.groovy')
      f = ProjectUtils.resolveSingleFile(project, 'logback.groovy')
    if(f == null && logbackConfigFile != 'logback.xml')
      f = ProjectUtils.resolveSingleFile(project, 'logback.xml')
    logbackConfigFile = f
    if(loggingLevel == null) loggingLevel = sourceConfig.loggingLevel ?: 'INFO'
    if(consoleLogEnabled == null) consoleLogEnabled = (sourceConfig.consoleLogEnabled == null ? true : sourceConfig.consoleLogEnabled)
    if(fileLogEnabled == null) fileLogEnabled = (sourceConfig.fileLogEnabled == null ? true : sourceConfig.fileLogEnabled)
    if(logFileName == null) logFileName = sourceConfig.logFileName ?: project.name
    if(logDir == null) logDir = sourceConfig.logDir ?: "${System.getProperty('user.home')}/logs"
    if(onStart == null) onStart = sourceConfig.onStart
    if(onStop == null) onStop = sourceConfig.onStop
    if(onScan == null) onScan = sourceConfig.onScan
    if(onScanFilesChanged == null) onScanFilesChanged = sourceConfig.onScanFilesChanged
  }
}


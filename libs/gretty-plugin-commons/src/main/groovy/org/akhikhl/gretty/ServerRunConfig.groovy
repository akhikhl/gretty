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

  private static void resolveJettyXmlFile(List<Project> projects, jettyXmlFile) {
    def f = projects.findResult { ProjectUtils.resolveSingleFile(it, jettyXmlFile) }
    if(f == null) {
      f = jettyXmlFile
      if(!(f instanceof File))
        f = new File(f)
      if(!f.isAbsolute()) {
        String jettyHome = System.getenv('JETTY_HOME')
        if(!jettyHome)
          jettyHome = System.getProperty('jetty.home')
        if(jettyHome != null) {
          File file = new File(new File(jettyHome, 'etc'), f.path)
          if(file.exists())
            f = file
        }
      }
    }
    f?.absolutePath
  }

  protected void setupProperties(List<Project> projects, ServerConfig sourceConfig) {
    if(jvmArgs == null) jvmArgs = sourceConfig.jvmArgs
    if(port == null) port = sourceConfig.port ?: 8080
    if(servicePort == null) servicePort = sourceConfig.servicePort ?: 9900
    if(statusPort == null) statusPort = sourceConfig.statusPort ?: 9901
    if(jettyXmlFile == null) jettyXmlFile = sourceConfig.jettyXmlFile ?: 'jetty.xml'
    jettyXmlFile = resolveJettyXmlFile(projects, jettyXmlFile)
    if(scanInterval == null) scanInterval = sourceConfig.scanInterval
    if(logbackConfigFile == null) logbackConfigFile = sourceConfig.logbackConfigFile
    if(loggingLevel == null) loggingLevel = sourceConfig.loggingLevel ?: 'INFO'
    if(consoleLogEnabled == null) consoleLogEnabled = (sourceConfig.consoleLogEnabled == null ? true : sourceConfig.consoleLogEnabled)
    if(fileLogEnabled == null) fileLogEnabled = (sourceConfig.fileLogEnabled == null ? true : sourceConfig.fileLogEnabled)
    if(logFileName == null) logFileName = sourceConfig.logFileName
    if(logDir == null) logDir = sourceConfig.logDir ?: "${System.getProperty('user.home')}/logs"
    if(onStart == null) onStart = sourceConfig.onStart
    if(onStop == null) onStop = sourceConfig.onStop
    if(onScan == null) onScan = sourceConfig.onScan
    if(onScanFilesChanged == null) onScanFilesChanged = sourceConfig.onScanFilesChanged
  }
}


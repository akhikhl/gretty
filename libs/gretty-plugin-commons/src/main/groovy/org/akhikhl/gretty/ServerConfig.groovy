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
class ServerConfig {

  List<String> jvmArgs
  Integer port
  Integer servicePort
  Integer statusPort
  def jettyXmlFile
  Integer scanInterval
  String logbackConfigFile
  String loggingLevel
  Boolean consoleLogEnabled
  Boolean fileLogEnabled
  String logFileName
  String logDir
  List<Closure> onStart
  List<Closure> onStop
  List<Closure> onScan
  List<Closure> onScanFilesChanged

  protected static ServerConfig getDefault(Project project) {
    ServerConfig result = new ServerConfig()
    result.jvmArgs = []
    result.port = 8080
    result.servicePort = 9900
    result.statusPort = 9901
    result.jettyXmlFile = 'jetty.xml'
    result.scanInterval = 0
    result.loggingLevel = 'INFO'
    result.consoleLogEnabled = true
    result.fileLogEnabled = true
    result.logFileName = project.name
    result.logDir = "${System.getProperty('user.home')}/logs" as String
    return result
  }

  void onScan(Closure newValue) {
    if(onScan == null)
      onScan = []
    onScan.add newValue
  }

  void onScanFilesChanged(Closure newValue) {
    if(onScanFilesChanged == null)
      onScanFilesChanged = []
    onScanFilesChanged.add newValue
  }

  void onStart(Closure newValue) {
    if(onStart == null)
      onStart = []
    onStart.add newValue
  }

  void onStop(Closure newValue) {
    if(onStop == null)
      onStop = []
    onStop.add newValue
  }

  protected void resolve(Project project) {
    def f = ProjectUtils.resolveSingleFile(project, jettyXmlFile)
    if(f == null && jettyXmlFile) {
      def f2 = jettyXmlFile
      if(!(f2 instanceof File))
        f2 = new File(f2)
      if(!f2.isAbsolute()) {
        String jettyHome = System.getenv('JETTY_HOME')
        if(!jettyHome)
          jettyHome = System.getProperty('jetty.home')
        if(jettyHome != null) {
          File f3 = new File(new File(jettyHome, 'etc'), f2.path)
          if(f3.exists())
            f = f3
        }
      }
    }
    jettyXmlFile = f
    f = ProjectUtils.resolveSingleFile(project, logbackConfigFile)
    if(f == null && logbackConfigFile != 'logback.groovy')
      f = ProjectUtils.resolveSingleFile(project, 'logback.groovy')
    if(f == null && logbackConfigFile != 'logback.xml')
      f = ProjectUtils.resolveSingleFile(project, 'logback.xml')
    logbackConfigFile = f

  }
}

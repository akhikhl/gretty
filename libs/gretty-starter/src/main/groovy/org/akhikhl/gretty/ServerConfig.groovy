 /*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
class ServerConfig {

  List<String> jvmArgs
  Map<String, String> systemProperties
  String servletContainer
  Boolean managedClassReload
  String host
  Boolean httpEnabled
  Integer httpPort
  Integer httpIdleTimeout
  Boolean httpsEnabled
  Integer httpsPort
  Integer httpsIdleTimeout
  def sslKeyStorePath
  String sslKeyStorePassword
  String sslKeyManagerPassword
  def sslTrustStorePath
  String sslTrustStorePassword
  Integer servicePort
  Integer statusPort
  def jettyXmlFile
  Integer scanInterval
  def logbackConfigFile
  String loggingLevel
  Boolean consoleLogEnabled
  Boolean fileLogEnabled
  def logFileName
  def logDir
  List<Closure> onStart
  List<Closure> onStop
  List<Closure> onScan
  List<Closure> onScanFilesChanged

  Integer getPort() {
    httpPort
  }

  void jvmArg(Object a) {
    if(args) {
      if(jvmArgs == null)
        jvmArgs = []
      jvmArgs.add(args)
    }
  }

  void jvmArgs(Object... args) {
    if(args) {
      if(jvmArgs == null)
        jvmArgs = []
      jvmArgs.addAll(args)
    }
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

  void setPort(Integer newValue) {
    httpPort = newValue
  }

  void systemProperty(String name, Object value) {
    if(systemProperties == null)
      systemProperties = [:]
    systemProperties[name] = value
  }

  void systemProperties(Map<String, Object> m) {
    if(m) {
      if(systemProperties == null)
        systemProperties = [:]
      systemProperties << m
    }
  }
}

 /*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.transform.ToString

/**
 *
 * @author akhikhl
 */
@ToString
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
  Boolean secureRandom
  String springBootMainClass

  static ServerConfig getDefaultServerConfig(String serverName) {
    ServerConfig result = new ServerConfig()
    result.jvmArgs = []
    result.servletContainer = 'jetty9'
    result.managedClassReload = true
    result.host = 'localhost'
    result.httpEnabled = true
    result.httpPort = 8080
    // httpIdleTimeout defaults to null. This means: no idle timeout is set for http protocol.
    result.httpsEnabled = false
    result.httpsPort = 8443
    // httpsIdleTimeout defaults to null. This means: no idle timeout is set for https protocol.
    result.servicePort = 9900
    result.statusPort = 9901
    result.jettyXmlFile = 'jetty.xml'
    result.scanInterval = 1
    result.loggingLevel = 'INFO'
    result.consoleLogEnabled = true
    result.fileLogEnabled = true
    result.logFileName = serverName
    result.logDir = "${System.getProperty('user.home')}/logs" as String
    return result
  }

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

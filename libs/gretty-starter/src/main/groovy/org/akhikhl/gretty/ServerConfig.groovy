/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
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

  static final int defaultServicePort = 9000
  static final int defaultStatusPort = 9001

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
  String sslHost
  def sslKeyStorePath
  String sslKeyStorePassword
  String sslKeyManagerPassword
  def sslTrustStorePath
  String sslTrustStorePassword
  def realm
  def realmConfigFile
  def serverConfigFile
  String reload
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
  Integer servicePort
  Integer statusPort
  Boolean secureRandom
  String springBootMainClass
  Boolean singleSignOn

  static ServerConfig getDefaultServerConfig(String serverName) {
    ServerConfig result = new ServerConfig()
    result.jvmArgs = []
    result.servletContainer = 'jetty9'
    result.managedClassReload = false
    result.httpEnabled = true
    result.httpsEnabled = false
    result.servicePort = defaultServicePort
    result.statusPort = defaultStatusPort
    result.reload = 'automatic'
    result.scanInterval = 1
    result.loggingLevel = 'INFO'
    result.consoleLogEnabled = true
    result.fileLogEnabled = true
    result.logFileName = serverName
    result.logDir = "${System.getProperty('user.home')}/logs" as String
    return result
  }

  // use serverConfigFile instead
  @Deprecated
  def getJettyXmlFile() {
    serverConfigFile
  }

  Integer getPort() {
    httpPort
  }

  void jvmArg(Object a) {
    if(a) {
      if(jvmArgs == null)
        jvmArgs = []
      jvmArgs.add(a)
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

  // use serverConfigFile instead
  @Deprecated
  void setJettyXmlFile(newValue) {
    serverConfigFile = newValue
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

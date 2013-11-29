/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.GradleException

class GrettyPluginExtension {

  int port = 8080
  int servicePort = 9900
  String contextPath
  Map initParameters = [:]
  String realm
  String realmConfigFile
  List overlays = []
  List<Closure> onStart = []
  List<Closure> onStop = []
  List<Closure> onScan = []
  List<Closure> onScanFilesChanged = []

  def initParameter(key, value) {
    initParameters[key] = value
  }

  def overlay(def newValue) {
    if(!(newValue instanceof String))
      throw new GradleException("Overlay ${newValue?.toString()} should be a string")
    overlays.add newValue
  }

  def onScan(Closure newValue) {
    onScan.add newValue
  }

  def onScanFilesChanged(Closure newValue) {
    onScanFilesChanged.add newValue
  }

  def onStart(Closure newValue) {
    onStart.add newValue
  }

  def onStop(Closure newValue) {
    onStop.add newValue
  }
}

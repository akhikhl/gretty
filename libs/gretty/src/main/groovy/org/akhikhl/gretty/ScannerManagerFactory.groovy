/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class ScannerManagerFactory {

  static ScannerManagerBase createScannerManager(Project project, ServerConfig sconfig) {
    int jettyVersion = sconfig.jettyVersion ?: 9
    def ScannerManagerClass
    switch(jettyVersion) {
      case 7:
        def classLoader = new URLClassLoader([ project.configurations.grettyUtil7.singleFile.toURI().toURL() ])
        ScannerManagerClass = Class.forName('org.akhikhl.gretty7.ScannerManager', true, classLoader)
        break
      case 8:
        def classLoader = new URLClassLoader([ project.configurations.grettyUtil8.singleFile.toURI().toURL() ])
        ScannerManagerClass = Class.forName('org.akhikhl.gretty8.ScannerManager', true, classLoader)
        break
      case 9:
        def classLoader = new URLClassLoader([ project.configurations.grettyUtil9.singleFile.toURI().toURL() ])
        ScannerManagerClass = Class.forName('org.akhikhl.gretty9.ScannerManager', true, classLoader)
        break
      default:
        throwUnsupportedJettyVersion(jettyVersion)
    }
    def scannerManager = ScannerManagerClass.newInstance()
    scannerManager.managedClassReload = sconfig.managedClassReload as boolean
  }
}


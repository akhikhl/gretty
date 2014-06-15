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

  static ScannerManagerBase createScannerManager(Project project, String servletContainer, boolean managedClassReload) {
    def servletContainerConfig = ServletContainerConfig.getConfig(servletContainer)
    def utilLibs = project.configurations[servletContainerConfig.grettyUtilConfig].files.collect { it.toURI().toURL() }
    /* getResolvedConfiguration().getFirstLevelModuleDependencies().collect { 
      it.getModuleArtifacts().find { it.getExtension() == 'jar' && !it.getClassifier() }.getFile().toURI().toURL()
    } */
    def classLoader = new URLClassLoader(utilLibs as URL[], ScannerManagerFactory.classLoader)
    def ScannerManagerClass = Class.forName("${servletContainerConfig.grettyUtilPackage}.ScannerManager", true, classLoader)
    def scannerManager = ScannerManagerClass.newInstance()
    scannerManager.managedClassReload = managedClassReload
    scannerManager
  }
}

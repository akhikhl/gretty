/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.eclipse.jetty.util.Scanner
import org.eclipse.jetty.util.Scanner.BulkListener
import org.eclipse.jetty.util.Scanner.ScanCycleListener
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class ScannerManager {

  private static final Logger log = LoggerFactory.getLogger(ScannerManager)

  private Scanner scanner

  def addScannerBulkListener(Closure listener) {
    scanner.addListener(new BulkListener() {
      void filesChanged(List<String> filenames) {
        listener.call(filenames)
      }
    });
  }

  def addScannerScanCycleListener(Closure listener) {
    scanner.addListener(new ScanCycleListener() {
      void scanEnded(int cycle) {
        listener.call(false, cycle)
      }
      void scanStarted(int cycle) {
        listener.call(true, cycle)
      }
    });
  }

  void startScanner(Project project, boolean inplace) {
    if(project.gretty.scanInterval == 0) {
      log.warn 'scanInterval not specified (or zero), scanning disabled'
      return
    }
    List<File> scanDirs = []
    if(inplace) {
      scanDirs.addAll project.sourceSets.main.runtimeClasspath.files
      scanDirs.add project.webAppDir
      for(def overlay in project.gretty.overlays) {
        overlay = project.project(overlay)
        scanDirs.addAll overlay.sourceSets.main.runtimeClasspath.files
        scanDirs.add overlay.webAppDir
      }
    } else {
      scanDirs.add project.tasks.war.archivePath
      scanDirs.add ProjectUtils.getFinalWarPath(project)
      for(def overlay in project.gretty.overlays)
        scanDirs.add ProjectUtils.getFinalWarPath(project.project(overlay))
    }
    for(def dir in project.gretty.scanDirs) {
      if(!(dir instanceof File))
        dir = project.file(dir.toString())
      scanDirs.add dir
    }
    for(File f in scanDirs)
      log.debug 'scanDir: {}', f
    scanner = new Scanner()
    scanner.reportDirs = true
    scanner.reportExistingFilesOnStartup = false
    scanner.scanInterval = project.gretty.scanInterval
    scanner.recursive = true
    scanner.scanDirs = scanDirs
    addScannerScanCycleListener { started, cycle ->
      log.debug 'ScanCycleListener started={}, cycle={}', started, cycle
      project.gretty.onScan*.call()
    }
    addScannerBulkListener { changedFiles ->
      log.debug 'BulkListener changedFiles={}', changedFiles
      project.gretty.onScanFilesChanged*.call(changedFiles)
      if(inplace)
        ProjectUtils.prepareInplaceWebAppFolder(project)
      else if(project.gretty.overlays) {
        ProjectUtils.prepareExplodedWebAppFolder(project)
        project.ant.zip destfile: project.ext.finalWarPath,  basedir: "${project.buildDir}/explodedWebapp"
      }
      else
        project.tasks.war.execute()
      ServiceControl.send(project.gretty.servicePort, 'restart')
    }
    log.info 'Starting scanner with interval of {} second(s)', project.gretty.scanInterval
    scanner.start()
  }

  void stopScanner() {
    if(scanner != null) {
      log.info 'Stopping scanner'
      scanner.stop()
      scanner = null
    }
  }
}


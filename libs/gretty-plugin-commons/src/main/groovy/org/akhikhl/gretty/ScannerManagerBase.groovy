 /*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.nio.file.Path
import java.nio.file.Paths
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class ScannerManagerBase {

  private static final Logger log = LoggerFactory.getLogger(ScannerManagerBase)

  protected GrettyStartTask startTask
  protected Project project
  protected boolean inplace
  protected scanner
  protected List<FastReloadStruct> fastReloadDirs = []

  protected abstract void addScannerBulkListener(Closure listener)

  private void configureFastReload() {
    if(inplace)
      fastReloadDirs = ProjectUtils.getFastReload(project, startTask.fastReload)
  }

  protected void configureScanner() {
    scanner.reportExistingFilesOnStartup = false
    scanner.scanInterval = startTask.scanInterval
    addScannerBulkListener { Collection<String> changedFiles ->
      for(def f in changedFiles)
        log.debug 'changedFile={}', f
      startTask.onScanFilesChanged*.call(changedFiles)
      if(inplace) {
        if(shouldFullReload(changedFiles)) {
          log.warn 'performing fullReload'
          ProjectUtils.prepareInplaceWebAppFolder(project)
          ServiceControl.send(startTask.servicePort, 'restart')
        } else {
          log.warn 'performing fastReload'
          ProjectUtils.prepareInplaceWebAppFolder(project)
        }
      }
      else if(project.gretty.overlays) {
        ProjectUtils.prepareExplodedWebAppFolder(project)
        project.ant.zip destfile: project.ext.finalWarPath,  basedir: "${project.buildDir}/explodedWebapp"
        ServiceControl.send(startTask.servicePort, 'restart')
      }
      else {
        project.tasks.war.execute()
        ServiceControl.send(startTask.servicePort, 'restart')
      }
    }
  }

  protected abstract createScanner()

  private void collectScanDirs(List<File> scanDirs, Project proj) {
    if(inplace) {
      scanDirs.addAll proj.sourceSets.main.runtimeClasspath.files
      scanDirs.add proj.webAppDir
    } else {
      scanDirs.add proj.tasks.war.archivePath
      scanDirs.add ProjectUtils.getFinalWarPath(proj)
    }
    for(def overlay in proj.gretty.overlays)
      collectScanDirs(scanDirs, proj.project(overlay))
  }

  private List<File> getEffectiveScanDirs() {
    List<File> scanDirs = []
    collectScanDirs(scanDirs, project)
    for(def dir in startTask.scanDirs) {
      if(!(dir instanceof File))
        dir = project.file(dir.toString())
      scanDirs.add dir
    }
    for(File f in scanDirs)
      log.debug 'scanDir: {}', f
    return scanDirs
  }

  protected boolean shouldFullReload(Collection<String> changedFiles) {
    for(String f in changedFiles) {
      if(f.endsWith('.jar')) {
        log.debug 'file {} is jar, switching to fullReload', f
        return true
      }
      if(f.endsWith('.class')) {
        log.debug 'file {} is class, switching to fullReload', f
        return true
      }
      if(['web.xml', 'web-fragment.xml', 'jetty.xml', 'jetty-env.xml'].find { f.endsWith(File.separator + it) }) {
        log.debug 'file {} is configuration file, switching to fullReload', f
        return true
      }
      if(f.startsWith(new File(project.webAppDir, 'WEB-INF/lib').absolutePath)) {
        log.debug 'file {} is in WEB-INF/lib, switching to fullReload', f
        return true
      }
      Path path = Paths.get(f)
      if(!fastReloadDirs.find { FastReloadStruct s ->
        String relPath = s.baseDir.toPath().relativize(path)
        f.startsWith(s.baseDir.absolutePath) && (s.pattern == null || relPath =~ s.pattern) && (s.excludesPattern == null || !(relPath =~ s.excludesPattern))
      }) {
        log.debug 'file {} is not in fastReloadDirs, switching to fullReload', f
        return true
      }
    }
    return false
  }

  final void startScanner(GrettyStartTask startTask, boolean inplace) {
    this.startTask = startTask
    this.project = startTask.project
    this.inplace = inplace
    if(startTask.scanInterval == 0) {
      log.warn 'scanInterval not specified (or zero), scanning disabled'
      return
    }
    scanner = createScanner()
    scanner.scanDirs = getEffectiveScanDirs()
    configureFastReload()
    configureScanner()
    log.warn 'Starting scanner with interval of {} second(s)', startTask.scanInterval
    scanner.start()
  }

  final void stopScanner() {
    if(scanner != null) {
      log.info 'Stopping scanner'
      scanner.stop()
      scanner = null
    }
  }
}

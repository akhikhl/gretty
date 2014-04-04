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

  private static class FastReloadStruct {
    File baseDir
    def pattern
    def excludesPattern
  }

  protected Project project
  protected boolean inplace
  protected scanner
  protected List<FastReloadStruct> fastReloadDirs = []

  private void addDefaultFastReloadDirs(Project proj) {
    fastReloadDirs.add(new FastReloadStruct(baseDir: proj.webAppDir))
    for(def overlay in proj.gretty.overlays)
      addDefaultFastReloadDirs(proj.project(overlay))
  }

  private void addFastReloadDirs(Project proj, List fastReloads) {
    for(def f in fastReloads) {
      if(f instanceof Boolean)
        continue
      File baseDir
      def pattern
      def excludesPattern
      if(f instanceof String)
        baseDir = new File(f)
      else if(f instanceof File)
        baseDir = f
      else if(f instanceof Map) {
        f.each { key, value ->
          if(key == 'baseDir')
            baseDir = value
          else if(key == 'pattern')
            pattern = value
          else if(key == 'excludesPattern')
            excludesPattern = value
          else
            log.warn 'Unknown fastReload property: {}', key
        }
        if(!baseDir) {
          log.warn 'fastReload property baseDir is not specified'
          continue
        }
      } else {
        log.warn 'fastReload argument must be String, File or Map'
        continue
      }
      ProjectUtils.resolveFile(proj, baseDir).each {
        fastReloadDirs.addAll(new FastReloadStruct(baseDir: it, pattern: pattern, excludesPattern: excludesPattern))
      }
    }
  }

  protected abstract void addScannerBulkListener(Closure listener)

  private List collectFastReloads(Project proj) {
    List result = []
    result.addAll(proj.gretty.fastReload)
    for(def overlay in proj.gretty.overlays.reverse()) {
      overlay = proj.project(overlay)
      if(overlay.extensions.findByName('gretty'))
        result.addAll(collectFastReloads(overlay))
    }
    return result
  }

  private void configureFastReload() {
    if(inplace) {
      List fastReloads = collectFastReloads(project)
      log.debug 'fastReloads={}', fastReloads
      fastReloadDirs = []
      if(fastReloads.find { (it instanceof Boolean) && it })
        addDefaultFastReloadDirs(project)
      addFastReloadDirs(project, fastReloads)
    }
  }

  protected void configureScanner() {
    scanner.reportExistingFilesOnStartup = false
    scanner.scanInterval = project.gretty.scanInterval
    addScannerBulkListener { Collection<String> changedFiles ->
      for(def f in changedFiles)
        log.debug 'changedFile={}', f
      project.gretty.onScanFilesChanged*.call(changedFiles)
      if(inplace) {
        if(shouldFullReload(changedFiles)) {
          log.warn 'performing fullReload'
          ProjectUtils.prepareInplaceWebAppFolder(project)
          ServiceControl.send(project.gretty.servicePort, 'restart')
        } else {
          log.warn 'performing fastReload'
          ProjectUtils.prepareInplaceWebAppFolder(project)
        }
      }
      else if(project.gretty.overlays) {
        ProjectUtils.prepareExplodedWebAppFolder(project)
        project.ant.zip destfile: project.ext.finalWarPath,  basedir: "${project.buildDir}/explodedWebapp"
        ServiceControl.send(project.gretty.servicePort, 'restart')
      }
      else {
        project.tasks.war.execute()
        ServiceControl.send(project.gretty.servicePort, 'restart')
      }
    }
  }

  protected abstract createScanner()

  private List<File> getEffectiveScanDirs() {
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

  final void startScanner(Project project, boolean inplace) {
    this.project = project
    this.inplace = inplace
    if(project.gretty.scanInterval == 0) {
      log.warn 'scanInterval not specified (or zero), scanning disabled'
      return
    }
    scanner = createScanner()
    scanner.scanDirs = getEffectiveScanDirs()
    configureFastReload()
    configureScanner()
    log.warn 'Starting scanner with interval of {} second(s)', project.gretty.scanInterval
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

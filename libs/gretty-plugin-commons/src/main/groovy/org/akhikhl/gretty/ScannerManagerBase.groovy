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

  protected ServerConfig sconfig
  protected List<WebAppRunConfig> webapps
  //protected boolean inplace
  protected scanner
  protected Map fastReload

  protected abstract void addScannerBulkListener(Closure listener)

  private void configureFastReload() {
    fastReload = [:]
    for(WebAppRunConfig webapp in webapps)
      if(webapp.inplace)
        fastReload[webapp.project.path] = ProjectUtils.getFastReload(webapp.project, webapp.fastReload)
  }

  protected void configureScanner() {
    scanner.reportExistingFilesOnStartup = false
    scanner.scanInterval = sconfig.scanInterval
    addScannerBulkListener { Collection<String> changedFiles ->
      scanFilesChanged(changedFiles)
    }
  }

  private static void collectScanDirs(List<File> scanDirs, boolean inplace, Project proj) {
    if(inplace) {
      scanDirs.addAll(proj.sourceSets.main.runtimeClasspath.files)
      scanDirs.add(proj.webAppDir)
    } else {
      scanDirs.add(proj.tasks.war.archivePath)
      scanDirs.add(ProjectUtils.getFinalWarPath(proj))
    }
    for(def overlay in proj.gretty.overlays)
      collectScanDirs(scanDirs, inplace, proj.project(overlay))
  }

  protected abstract createScanner()

  private List<File> getEffectiveScanDirs() {
    List<File> scanDirs = []
    for(WebAppRunConfig webapp in webapps) {
      collectScanDirs(scanDirs, webapp.inplace, webapp.project)
      for(def dir in webapp.scanDirs) {
        if(!(dir instanceof File))
          dir = project.file(dir.toString())
        scanDirs.add(dir)
      }
    }
    for(File f in scanDirs)
      log.debug 'scanDir: {}', f
    return scanDirs
  }

  protected void scanFilesChanged(Collection<String> changedFiles) {

    for(def f in changedFiles)
      log.warn 'changedFile={}', f

    sconfig.onScanFilesChanged*.call(changedFiles)

    List<WebAppRunConfig> changedWebApps = []
    boolean fastReload = true

    for(String f in changedFiles) {
      if(f.endsWith('.jar')) {
        List<WebAppRunConfig> dependantWebApps = webapps.findAll { it.project.configurations.compile.dependencies.find { it.absolutePath == f } }
        if(dependantWebApps) {
          log.warn 'file {} is dependency of {}, switching to fullReload', f, dependantWebApps[0].project
          fastReload = false
        }
       log.warn 'changed file {} affects projects {}', (dependantWebApps.collect { it.project.name })
        changedWebApps += dependantWebApps
        continue
      }
      WebAppRunConfig webapp = webapps.find { f.startsWith(it.project.projectDir.absolutePath) }
      if(webapp != null) {
        log.warn 'changed file {} affects project {}', webapp.project.name
        changedWebApps.add(webapp)
        if(f.endsWith('.class')) {
          log.warn 'file {} is class, switching to fullReload', f
          fastReload = false
        }
        else if(['web.xml', 'web-fragment.xml', 'jetty.xml', 'jetty-env.xml'].find { it == new File(f).name }) {
          log.warn 'file {} is configuration file, switching to fullReload', f
          fastReload = false
        }
        else if(f.startsWith(new File(webapp.project.webAppDir, 'WEB-INF/lib').absolutePath)) {
          log.warn 'file {} is in WEB-INF/lib, switching to fullReload', f
          fastReload = false
        } else {
          List<FastReloadStruct> fastReloadDirs = fastReload[webapp.project.path]
          if(!fastReloadDirs?.find { FastReloadStruct s ->
            String relPath = s.baseDir.toPath().relativize(Paths.get(f))
            f.startsWith(s.baseDir.absolutePath) && (s.pattern == null || relPath =~ s.pattern) && (s.excludesPattern == null || !(relPath =~ s.excludesPattern))
          }) {
            log.warn 'file {} is not in fastReload directories, switching to fullReload', f
            fastReload = false
          }
        }
      }
    }

    if(fastReload) {
      log.warn 'performing fastReload'
      for(WebAppRunConfig webapp in changedWebApps)
        ProjectUtils.prepareInplaceWebAppFolder(webapp.project)
    } else {
      log.warn 'performing fullReload'
      for(WebAppRunConfig webapp in changedWebApps) {
        if(webapp.inplace)
          ProjectUtils.prepareInplaceWebAppFolder(webapp.project)
        else if(webapp.project.gretty.overlays) {
          // TODO: check how it works
          ProjectUtils.prepareExplodedWebAppFolder(webapp.project)
          webapp.project.ant.zip destfile: webapp.project.ext.finalWarPath,  basedir: "${webapp.project.buildDir}/explodedWebapp"
        }
        else {
          // TODO: check how it works
          webapp.project.tasks.war.execute()
        }
      }
      ServiceControl.send(sconfig.servicePort, 'restart')
    }
  }

  final void startScanner(ServerConfig sconfig, List<WebAppRunConfig> webapps) {
    this.sconfig = sconfig
    this.webapps = webapps
    if(sconfig.scanInterval == 0) {
      log.warn 'scanInterval not specified (or zero), scanning disabled'
      return
    }
    scanner = createScanner()
    scanner.scanDirs = getEffectiveScanDirs()
    configureFastReload()
    configureScanner()
    log.warn 'Starting scanner with interval of {} second(s)', sconfig.scanInterval
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

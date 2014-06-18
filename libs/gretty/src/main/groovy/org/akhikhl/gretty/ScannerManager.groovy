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
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class ScannerManager {

  private static final Logger log = LoggerFactory.getLogger(ScannerManager)

  protected Project project
  protected ServerConfig sconfig
  protected List<WebAppConfig> webapps
  protected scanner
  protected Map fastReloadMap
  protected boolean managedClassReload

  private static void collectScanDirs(Collection<File> scanDirs, boolean inplace, Project proj) {
    scanDirs.add(ProjectUtils.getWebAppDir(proj))
    scanDirs.addAll(proj.sourceSets.main.allSource.srcDirs)
    scanDirs.addAll(proj.sourceSets.main.runtimeClasspath.files)
    for(def overlay in proj.gretty.overlays)
      collectScanDirs(scanDirs, inplace, proj.project(overlay))
  }

  private void configureFastReload() {
    fastReloadMap = [:]
    for(WebAppConfig webapp in webapps)
      if(webapp.inplace && webapp.projectPath) {
        def proj = project.project(webapp.projectPath)
        fastReloadMap[webapp.projectPath] = ProjectUtils.getFastReload(proj, webapp.fastReload)
      }
  }

  protected void configureScanner() {

    scanner.reportExistingFilesOnStartup = false
    scanner.scanInterval = sconfig.scanInterval

    scanner.addListener(new BulkListener() {
      void filesChanged(List<String> filenames) {
        scanFilesChanged(filenames)
      }
    });

    scanner.reportDirs = true
    scanner.recursive = true

    scanner.addListener(new ScanCycleListener() {
      void scanEnded(int cycle) {
      }
      void scanStarted(int cycle) {
        sconfig.onScan*.call(cycle)
      }
    });
  }

  private List<File> getEffectiveScanDirs() {
    Set<File> scanDirs = new LinkedHashSet()
    for(WebAppConfig webapp in webapps) {
      if(webapp.projectPath) {
        def proj = project.project(webapp.projectPath)
        collectScanDirs(scanDirs, webapp.inplace, proj)
        for(def dir in webapp.scanDirs) {
          if(!(dir instanceof File))
            dir = proj.file(dir.toString())
          scanDirs.add(dir)
        }
      }
    }
    for(File f in scanDirs)
      log.debug 'scanDir: {}', f
    return scanDirs as List
  }

  protected void scanFilesChanged(Collection<String> changedFiles) {

    for(def f in changedFiles)
      log.debug 'changedFile={}', f

    sconfig.onScanFilesChanged*.call(changedFiles)

    Map<WebAppConfig> webAppProjectReloads = [:]

    def reloadProject = { String projectPath, String reloadMode ->
      if(webAppProjectReloads[projectPath] == null)
        webAppProjectReloads[projectPath] = new HashSet()
      webAppProjectReloads[projectPath] += reloadMode
    }

    boolean shouldRestart = false

    for(String f in changedFiles) {
      if(f.endsWith('.jar')) {
        List<WebAppConfig> dependantWebAppProjects = webapps.findAll {
          it.projectPath && project.project(it.projectPath).configurations.compile.dependencies.find { it.absolutePath == f }
        }
        if(dependantWebAppProjects) {
          for(WebAppConfig wconfig in dependantWebAppProjects) {
            log.debug 'changed file {} is dependency of {}, the latter will be recompiled', f, wconfig.projectPath
            reloadProject(wconfig.projectPath, 'compile')
          }
          shouldRestart = true
          continue
        }
      }
      WebAppConfig wconfig = webapps.find {
        it.projectPath && f.startsWith(project.project(it.projectPath).projectDir.absolutePath)
      }
      if(wconfig != null) {
        log.debug 'changed file {} affects project {}', f, wconfig.projectPath
        def proj = project.project(wconfig.projectPath)

        if (proj.sourceSets.main.allSource.srcDirs.find { f.startsWith(it.absolutePath) }) {
          reloadProject(wconfig.projectPath, 'compile')
          // restart is done when reacting in output change, not source change
        } else if (proj.sourceSets.main.output.files.find { f.startsWith(it.absolutePath) }) {
          if(managedClassReload) {
            log.debug 'file {} is in managed output of {}, jetty will not be restarted', f, wconfig.projectPath
          } else {
            log.debug 'file {} is in output of {}, jetty will be restarted', f, wconfig.projectPath
            shouldRestart = true
          }
        } else if (new File(f).name in ['web.xml', 'web-fragment.xml', 'jetty.xml', 'jetty-env.xml']) {
          log.debug 'file {} is configuration file, jetty will be restarted', f
          reloadProject(wconfig.projectPath, 'compile')
          shouldRestart = true
        }
        else if(f.startsWith(new File(ProjectUtils.getWebAppDir(proj), 'WEB-INF/lib').absolutePath)) {
          log.debug 'file {} is in WEB-INF/lib, jetty will be restarted', f
          reloadProject(wconfig.projectPath, 'compile')
          shouldRestart = true
        } else {
          List<FastReloadStruct> fastReloadDirs = fastReloadMap[proj.path]
          if(fastReloadDirs?.find { FastReloadStruct s ->
            String relPath = f - s.baseDir.absolutePath - File.separator
            f.startsWith(s.baseDir.absolutePath) && (s.pattern == null || relPath =~ s.pattern) && (s.excludesPattern == null || !(relPath =~ s.excludesPattern))
          }) {
            log.debug 'file {} is in fastReload directories', f
            reloadProject(wconfig.projectPath, 'fastReload')
          } else {
            log.debug 'file {} is not in fastReload directories, switching to fullReload', f
            reloadProject(wconfig.projectPath, 'compile')
            shouldRestart = true
          }
        }
      }
    }

    webAppProjectReloads.each { String projectPath, Set reloadModes ->
      Project proj = project.project(projectPath)
      if(reloadModes.contains('compile')) {
        log.warn 'Recompiling {}', (projectPath == ':' ? proj.name : projectPath)
        WebAppConfig wconfig = webapps.find { it.projectPath == projectPath }
        ProjectConnection connection = GradleConnector.newConnector().useInstallation(proj.gradle.gradleHomeDir).forProjectDirectory(proj.projectDir).connect()
        try {
          connection.newBuild().forTasks(wconfig.inplace ? 'prepareInplaceWebApp' : 'prepareArchiveWebApp').run()
        } finally {
          connection.close()
        }
      } else if(reloadModes.contains('fastReload')) {
        log.warn 'Fast-reloading {}', (projectPath == ':' ? proj.name : projectPath)
        ProjectUtils.prepareInplaceWebAppFolder(proj)
      }
    }

    if(shouldRestart)
      ServiceProtocol.send(sconfig.servicePort, 'restart')
  }

  final void startScanner(Project project, ServerConfig sconfig, List<WebAppConfig> webapps, boolean managedClassReload) {
    this.project = project
    this.sconfig = sconfig
    this.webapps = webapps
    this.managedClassReload = managedClassReload
    if(!sconfig.scanInterval) {
      if(sconfig.scanInterval == null)
        log.debug 'scanInterval not specified, hot deployment disabled'
      else if(sconfig.scanInterval == 0)
        log.debug 'scanInterval is zero, hot deployment disabled'
      return
    }
    scanner = new Scanner()
    scanner.scanDirs = getEffectiveScanDirs()
    configureFastReload()
    configureScanner()
    log.debug 'Enabling hot deployment with interval of {} second(s)', sconfig.scanInterval
    scanner.start()
  }

  final void stopScanner() {
    if(scanner != null) {
      log.info 'Stopping scanner'
      scanner.stop()
      scanner = null
      project = null
      sconfig = null
      webapps = null
      fastReloadMap = null
    }
  }
}

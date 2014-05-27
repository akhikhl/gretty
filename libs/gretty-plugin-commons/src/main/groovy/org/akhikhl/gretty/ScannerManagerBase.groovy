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
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class ScannerManagerBase {

  private static final Logger log = LoggerFactory.getLogger(ScannerManagerBase)

  protected Project project
  protected ServerConfig sconfig
  protected List<WebAppConfig> webapps
  //protected boolean inplace
  protected scanner
  protected Map fastReloadMap

  protected abstract void addScannerBulkListener(Closure listener)

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
    addScannerBulkListener { Collection<String> changedFiles ->
      scanFilesChanged(changedFiles)
    }
  }

  private static void collectScanDirs(Collection<File> scanDirs, boolean inplace, Project proj) {
    scanDirs.add(proj.webAppDir)
    scanDirs.addAll(proj.sourceSets.main.allSource.srcDirs)
    // do not scan build output, otherwise we are in a loop: scan -> build -> scan ...
    scanDirs.addAll(proj.sourceSets.main.runtimeClasspath.files - proj.sourceSets.main.output.files)
    for(def overlay in proj.gretty.overlays)
      collectScanDirs(scanDirs, inplace, proj.project(overlay))
  }

  protected abstract createScanner()

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

    List<WebAppConfig> changedWebAppProjects = []
    boolean fastReload = true

    for(String f in changedFiles) {
      if(f.endsWith('.jar')) {
        List<WebAppConfig> dependantWebAppProjects = webapps.findAll {
          it.projectPath && project.project(it.projectPath).configurations.compile.dependencies.find { it.absolutePath == f }
        }
        if(dependantWebAppProjects) {
          log.debug 'changed file {} is dependency of {}, switching to fullReload', f, (dependantWebAppProjects.collect { it.projectPath })
          fastReload = false
        }
        log.debug 'changed file {} affects projects {}', f, (dependantWebAppProjects.collect { it.projectPath })
        changedWebAppProjects += dependantWebAppProjects
        continue
      }
      WebAppConfig webapp = webapps.find {
        it.projectPath && f.startsWith(project.project(it.projectPath).projectDir.absolutePath)
      }
      if(webapp != null) {
        log.debug 'changed file {} affects project {}', f, webapp.projectPath
        changedWebAppProjects.add(webapp)
        def proj = project.project(webapp.projectPath)
        if(f.endsWith('.class')) {
          log.debug 'file {} is class, switching to fullReload', f
          fastReload = false
        }
        else if(['web.xml', 'web-fragment.xml', 'jetty.xml', 'jetty-env.xml'].find { it == new File(f).name }) {
          log.debug 'file {} is configuration file, switching to fullReload', f
          fastReload = false
        }
        else if(f.startsWith(new File(proj.webAppDir, 'WEB-INF/lib').absolutePath)) {
          log.debug 'file {} is in WEB-INF/lib, switching to fullReload', f
          fastReload = false
        } else {
          List<FastReloadStruct> fastReloadDirs = fastReloadMap[proj.path]
          if(!fastReloadDirs?.find { FastReloadStruct s ->
            String relPath = s.baseDir.toPath().relativize(Paths.get(f))
            f.startsWith(s.baseDir.absolutePath) && (s.pattern == null || relPath =~ s.pattern) && (s.excludesPattern == null || !(relPath =~ s.excludesPattern))
          }) {
            log.debug 'file {} is not in fastReload directories, switching to fullReload', f
            fastReload = false
          }
        }
      }
    }

    if(fastReload) {
      log.warn 'performing fastReload'
      for(WebAppConfig webapp in changedWebAppProjects) {
        def proj = project.project(webapp.projectPath)
        ProjectUtils.prepareInplaceWebAppFolder(proj)
      }
    } else {
      log.warn 'performing fullReload'
      for(WebAppConfig webapp in changedWebAppProjects) {
        Project proj = project.project(webapp.projectPath)
        ProjectConnection connection = GradleConnector.newConnector().useInstallation(proj.gradle.gradleHomeDir).forProjectDirectory(proj.projectDir).connect()
        try {
          connection.newBuild().forTasks(webapp.inplace ? 'prepareInplaceWebApp' : 'prepareWarWebApp').run()
        } finally {
          connection.close()
        }
      }
      ServiceProtocol.send(sconfig.servicePort, 'restart')
    }
  }

  final void startScanner(Project project, ServerConfig sconfig, List<WebAppConfig> webapps) {
    this.project = project
    this.sconfig = sconfig
    this.webapps = webapps
    if(!sconfig.scanInterval) {
      if(sconfig.scanInterval == null)
        log.warn 'scanInterval not specified, hot deployment disabled'
      else if(sconfig.scanInterval == 0)
        log.warn 'scanInterval is zero, hot deployment disabled'
      return
    }
    scanner = createScanner()
    scanner.scanDirs = getEffectiveScanDirs()
    configureFastReload()
    configureScanner()
    log.warn 'Enabling hot deployment with interval of {} second(s)', sconfig.scanInterval
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

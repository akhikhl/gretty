/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty
import org.akhikhl.gretty.scanner.BaseScannerManager
import org.eclipse.jetty.util.Scanner
import org.eclipse.jetty.util.Scanner.BulkListener
import org.eclipse.jetty.util.Scanner.ScanCycleListener
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class JettyScannerManager extends BaseScannerManager implements ScannerManager {
  private static final Logger log = LoggerFactory.getLogger(JettyScannerManager)

  protected Scanner scanner

  JettyScannerManager(Project project, ServerConfig sconfig, List<WebAppConfig> webapps, boolean managedClassReload) {
    super(project, sconfig, webapps, managedClassReload)
  }

  protected void configureScanner() {
    scanner.reportExistingFilesOnStartup = false
    scanner.scanInterval = sconfig.scanInterval

    scanner.addListener(new BulkListener() {
      void filesChanged(List<String> filenames) {
        scanFilesChangedOverride(filenames)
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

  /*
   * This method exists as a workaround for Groovy issue
   * https://issues.apache.org/jira/browse/GROOVY-7862 which is
   * fixed in Groovy 2.5.
   *
   * See https://github.com/gretty-gradle-plugin/gretty/issues/99 .
   */
  protected void scanFilesChangedOverride(Collection<String> changedFiles) {
	  super.scanFilesChanged(changedFiles)
  }

  @Override
  void startScanner() {
    if(!sconfig.scanInterval) {
      if(sconfig.scanInterval == null)
        log.info 'scanInterval not specified, hot deployment disabled'
      else if(sconfig.scanInterval == 0)
        log.info 'scanInterval is zero, hot deployment disabled'
      return
    }
    scanner = new Scanner()
    List<File> scanDirs = getEffectiveScanDirs()
    configureFastReload(scanDirs)
    for(File f in scanDirs)
      log.info 'scanDir: {}', f
    log.info 'fastReloadMap={}', fastReloadMap
    scanner.scanDirs = scanDirs
    configureScanner()
    log.info 'Enabling hot deployment with interval of {} second(s)', sconfig.scanInterval
    scanner.start()
  }

  @Override
  void stopScanner() {
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

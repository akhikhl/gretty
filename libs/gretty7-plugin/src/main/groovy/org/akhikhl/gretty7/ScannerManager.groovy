/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty7

import org.eclipse.jetty.util.Scanner
import org.eclipse.jetty.util.Scanner.BulkListener
import org.eclipse.jetty.util.Scanner.ScanCycleListener
import org.akhikhl.gretty.ScannerManagerBase

final class ScannerManager extends ScannerManagerBase {

  @Override
  protected void addScannerBulkListener(Closure listener) {
    scanner.addListener(new BulkListener() {
      void filesChanged(List<String> filenames) {
        listener.call(filenames)
      }
    });
  }

  private addScannerScanCycleListener(Closure listener) {
    scanner.addListener(new ScanCycleListener() {
      void scanEnded(int cycle) {
        listener.call(false, cycle)
      }
      void scanStarted(int cycle) {
        listener.call(true, cycle)
      }
    });
  }

  @Override
  protected void configureScanner() {
    super.configureScanner()
    scanner.reportDirs = true
    scanner.recursive = true
    addScannerScanCycleListener { started, cycle ->
      if(started)
        startTask.onScan*.call(cycle)
    }
  }

  @Override
  protected createScanner() {
    return new Scanner()
  }
}

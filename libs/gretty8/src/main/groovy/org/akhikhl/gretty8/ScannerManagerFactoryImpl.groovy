/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty8

import org.akhikhl.gretty.ScannerManagerBase
import org.akhikhl.gretty.ScannerManagerFactory

/**
 *
 * @author akhikhl
 */
class ScannerManagerFactoryImpl implements ScannerManagerFactory {
  
  boolean managedClassReload
  
  ScannerManagerFactoryImpl(boolean managedClassReload = false) {
    this.managedClassReload = managedClassReload
  }

  ScannerManagerBase createScannerManager() {
    def scannerManager = new ScannerManager()
    scannerManager.managedClassReload = managedClassReload
    scannerManager
  }
}

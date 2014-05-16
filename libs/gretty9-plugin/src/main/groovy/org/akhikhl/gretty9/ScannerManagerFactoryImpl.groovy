/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty9

import org.akhikhl.gretty.ScannerManagerBase
import org.akhikhl.gretty.ScannerManagerFactory

/**
 *
 * @author akhikhl
 */
class ScannerManagerFactoryImpl implements ScannerManagerFactory {

  static final ScannerManagerFactory instance = new ScannerManagerFactoryImpl()

  ScannerManagerBase createScannerManager() {
    new ScannerManager()
  }
}


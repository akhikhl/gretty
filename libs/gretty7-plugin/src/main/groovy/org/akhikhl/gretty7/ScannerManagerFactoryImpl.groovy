/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty7

import org.akhikhl.gretty.ScannerManagerBase
import org.akhikhl.gretty.ScannerManagerFactory

/**
 *
 * @author akhikhl
 */
@org.kordamp.jipsy.ServiceProviderFor(ScannerManagerFactory)
class ScannerManagerFactoryImpl implements ScannerManagerFactory {

  ScannerManagerBase createScannerManager() {
    new ScannerManager()
  }
}


/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty6

import org.mortbay.util.Scanner
import org.mortbay.util.Scanner.BulkListener
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

  @Override
  protected createScanner() {
    return new Scanner()
  }
}


/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
interface Launcher {
  
  ScannerManager getScannerManager()

  void launch()
  
  Thread launchThread()
  
  void setScannerManager(ScannerManager newValue)
}

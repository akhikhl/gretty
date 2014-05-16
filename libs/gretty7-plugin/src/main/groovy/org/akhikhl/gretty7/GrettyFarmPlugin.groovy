/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty7

import org.akhikhl.gretty.GrettyFarmPluginBase
import org.akhikhl.gretty.ScannerManagerFactory

/**
 *
 * @author akhikhl
 */
class GrettyFarmPlugin extends GrettyFarmPluginBase {

  @Override
  String getJettyVersion() {
    Messages.getString('jettyVersion')
  }

  @Override
  String getPluginName() {
    Messages.getString('farmPluginName')
  }

  @Override
  ScannerManagerFactory getScannerManagerFactory() {
    ScannerManagerFactoryImpl.instance
  }
}


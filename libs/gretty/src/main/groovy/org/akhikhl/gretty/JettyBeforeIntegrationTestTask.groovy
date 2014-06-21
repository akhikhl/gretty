/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This class is deprecated, please use AppBeforeIntegrationTestTask instead.
 *
 * @author akhikhl
 */
class JettyBeforeIntegrationTestTask extends AppBeforeIntegrationTestTask {

  protected static final Logger log = LoggerFactory.getLogger(JettyBeforeIntegrationTestTask)
	
  JettyBeforeIntegrationTestTask() {
    doFirst {
      log.warn 'JettyBeforeIntegrationTestTask is deprecated and will be removed in Gretty 2.0. Please use AppBeforeIntegrationTestTask instead.'
    }
  }
}

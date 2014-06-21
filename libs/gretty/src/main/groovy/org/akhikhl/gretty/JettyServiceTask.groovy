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
 *
 * @author akhikhl
 */
abstract class JettyServiceTask extends AppServiceTask {

  protected static final Logger log = LoggerFactory.getLogger(JettyBeforeIntegrationTestTask)
	
  JettyServiceTask() {
    doFirst {
      log.warn 'JettyServiceTask is deprecated and will be removed in Gretty 2.0. Please use AppServiceTask instead.'
    }
  }
}

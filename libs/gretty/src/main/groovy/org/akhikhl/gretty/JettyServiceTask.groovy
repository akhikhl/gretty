/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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

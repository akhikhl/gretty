/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.BeansException
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 *
 * @author akhikhl
 */
@Component
class ApplicationContextProvider implements ApplicationContextAware {
  
  private static final Logger log = LoggerFactory.getLogger(ApplicationContextProvider)
  
  private static ApplicationContext ctx
  
  static ApplicationContext getApplicationContext() {
    ctx
  }

  @Override
  void setApplicationContext(ApplicationContext ctx) throws BeansException {
    this.ctx = ctx
  }      
}

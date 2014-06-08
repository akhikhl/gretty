/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

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

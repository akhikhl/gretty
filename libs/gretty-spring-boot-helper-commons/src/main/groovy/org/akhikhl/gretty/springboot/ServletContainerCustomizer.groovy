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

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainer
import org.springframework.stereotype.Component

/**
 *
 * @author akhikhl
 */
@Component
class ServletContainerCustomizer implements EmbeddedServletContainerCustomizer {
  
  private static final Logger log = LoggerFactory.getLogger(ServletContainerCustomizer)
  
  protected static Map params
	
  void customize(ConfigurableEmbeddedServletContainer container) {
    if(container instanceof GrettyConfigurableServletContainerFactory) {
      GrettyConfigurableServletContainerFactory intf = container
      intf.setParams(params)
    }
  }
}


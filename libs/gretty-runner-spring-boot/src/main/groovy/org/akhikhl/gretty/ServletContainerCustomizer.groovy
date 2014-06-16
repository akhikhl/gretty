/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.stereotype.Component

/**
 *
 * @author akhikhl
 */
@Component
class ServletContainerCustomizer implements EmbeddedServletContainerCustomizer {
  
  protected static Map params
	
  void customize(ConfigurableEmbeddedServletContainer container) {
    if(container instanceof GrettyConfigurableServletContainerFactory) {
      GrettyConfigurableServletContainerFactory intf = container
      intf.setParams(params)
    }
  }
}


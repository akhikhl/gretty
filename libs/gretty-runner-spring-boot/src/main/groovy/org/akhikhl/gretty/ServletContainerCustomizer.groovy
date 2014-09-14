/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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


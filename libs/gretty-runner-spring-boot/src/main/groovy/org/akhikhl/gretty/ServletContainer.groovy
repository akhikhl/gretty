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
import org.springframework.boot.context.embedded.EmbeddedServletContainer
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
 *
 * @author akhikhl
 */
@Component
class ServletContainer {
  
  protected static EmbeddedServletContainer servletContainer
  
  @Bean
  def servletContainerInitializedEvent() {
    new ApplicationListener<EmbeddedServletContainerInitializedEvent>() {
      @Override
      public void onApplicationEvent(EmbeddedServletContainerInitializedEvent appEvent) {
        servletContainer = appEvent.getEmbeddedServletContainer()
      }      
    }
  }
}

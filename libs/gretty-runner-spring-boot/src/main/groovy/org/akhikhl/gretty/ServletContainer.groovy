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

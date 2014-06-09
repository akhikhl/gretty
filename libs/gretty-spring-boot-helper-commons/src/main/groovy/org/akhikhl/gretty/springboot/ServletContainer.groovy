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

import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainer
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

import org.springframework.context.ApplicationEvent

/**
 *
 * @author akhikhl
 */
@Component
class ServletContainer {
  
  protected static JettyEmbeddedServletContainer servletContainer
  
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

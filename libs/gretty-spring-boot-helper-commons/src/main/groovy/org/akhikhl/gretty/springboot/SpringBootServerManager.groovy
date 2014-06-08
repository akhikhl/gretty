/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import org.akhikhl.gretty.ServerManager
import org.akhikhl.gretty.LoggingUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainer
import org.springframework.context.ApplicationContext

/**
 *
 * @author akhikhl
 */
final class SpringBootServerManager implements ServerManager {
  
  protected static final Logger log = LoggerFactory.getLogger(SpringBootServerManager)
  
  protected Map params

  @Override
  void setParams(Map params) {
    this.params = params
  }
  
  @Override
  void startServer() {

    def springBootMainClass = Class.forName(params.springBootMainClass, true, this.getClass().classLoader)
    springBootMainClass.main([ '--spring.main.sources=org.akhikhl.gretty.springboot' ] as String[])
    
    log.warn 'spring-boot server started!'
    
    /*String[] beanNames = ApplicationContextProvider.applicationContext.getBeanDefinitionNames()
    Arrays.sort(beanNames)
    for (String beanName : beanNames)
      log.warn 'bean: {}', beanName*/
  }

  @Override
  void stopServer() {
    if(ServletContainerConfigurer.servletContainer != null) {
      ServletContainerConfigurer.servletContainer.stop()
      ServletContainerConfigurer.servletContainer = null
    }
    log.warn 'spring-boot server stopped!'
  }
}

/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.akhikhl.gretty.ServerManager
import org.akhikhl.gretty.LoggingUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication

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
    startServer(null)
  }

  @Override
  void startServer(ServerStartEvent startEvent) {

    if(params.logbackConfig)
      System.setProperty('logging.config', new File(params.logbackConfig).toURI().toURL().toString())

    params.startEvent = startEvent
    ServletContainerCustomizer.params = params

    def springBootMainClass = Class.forName(params.springBootMainClass, true, this.getClass().classLoader)

    def springBootSources = ([ 'org.akhikhl.gretty' ] + params.webApps.findResults { it.springBootSources }).join(',')

    springBootMainClass.main([ "--spring.main.sources=$springBootSources" ] as String[])

    if(log.isDebugEnabled()) {
      String[] beanNames = ApplicationContextProvider.applicationContext.getBeanDefinitionNames()
      Arrays.sort(beanNames)
      for (String beanName : beanNames)
        log.debug 'bean: {}', beanName
    }
  }

  @Override
  void stopServer() {
    SpringApplication.exit(ApplicationContextProvider.applicationContext)
  }
}

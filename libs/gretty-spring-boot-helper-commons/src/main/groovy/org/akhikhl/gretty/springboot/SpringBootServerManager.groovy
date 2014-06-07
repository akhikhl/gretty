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

/**
 *
 * @author akhikhl
 */
final class SpringBootServerManager implements ServerManager {
  
  protected Map params
	protected server
  protected Logger log 
  
  SpringBootServerManager() {
  }

  @Override
  void setParams(Map params) {
    this.params = params
  }
 
  @Override
  void startServer() {
    assert server == null

    if(params.logging)
      LoggingUtils.configureLogging(params.logging)
    else if(params.logbackConfig)
      LoggingUtils.useConfig(params.logbackConfig)

    log = LoggerFactory.getLogger(this.getClass())
    
    //configurer.setLogger(log)
    def webapp = params.webApps[0]
    log.warn 'webapp.webappClassPath: {}', webapp.webappClassPath
    URL[] classpathUrls = (webapp.webappClassPath.collect { new URL(it) }) as URL[]
    classpathUrls.each {
      log.warn 'URL {}', it
    }
    ClassLoader classLoader = new URLClassLoader(classpathUrls, this.getClass().getClassLoader())
    Class.forName('org.springframework.boot.autoconfigure.EnableAutoConfigurationImportSelector', true, classLoader)
    
    def springBootMainClass = Class.forName(params.springBootMainClass, true, classLoader)
    assert springBootMainClass != null
    springBootMainClass.main([] as String[])
    
    log.warn 'spring-boot server started!'
  }

  @Override
  void stopServer() {
    if(server != null) {
      server.stop()
      server = null
    }
    log.warn 'spring-boot server stopped!'
    log = null
  }
}

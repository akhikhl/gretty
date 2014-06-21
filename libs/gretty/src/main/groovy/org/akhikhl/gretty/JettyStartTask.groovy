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

/**
 *
 * @author akhikhl
 */
class JettyStartTask extends AppStartTask {

  protected static final Logger log = LoggerFactory.getLogger(JettyStartTask)

  @Override
  protected String getCompatibleServletContainer(String servletContainer) {
    def config = ServletContainerConfig.getConfig(servletContainer)
    if(config.servletContainerType == 'jetty')
      return servletContainer
    def compatibleConfigEntry = ServletContainerConfig.getConfigs.find { name, c ->
      c.servletContainerType == 'jetty' && c.servletApiVersion == config.servletApiVersion
    }
    if(compatibleConfigEntry)
      return compatibleConfig.key
    String defaultJettyServletContainer = 'jetty9'
    log.warn 'Cannot find jetty container with compatible servlet-api to {}, defaulting to {}', servletContainer, defaultJettyServletContainer
    defaultJettyServletContainer
  }
}

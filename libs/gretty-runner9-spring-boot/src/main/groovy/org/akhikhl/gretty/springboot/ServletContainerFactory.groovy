/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import org.akhikhl.gretty.Jetty9Configurer
import org.akhikhl.gretty.ServerConfigurer
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.servlet.ServletMapping
import org.eclipse.jetty.webapp.Configuration
import org.eclipse.jetty.webapp.WebAppContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.embedded.EmbeddedServletContainer
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer
import org.springframework.boot.context.embedded.ServletContextInitializer
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils

/**
 *
 * @author akhikhl
 */
@Component('jettyEmbeddedServletContainerFactory')
class ServletContainerFactory extends JettyEmbeddedServletContainerFactory implements GrettyConfigurableServletContainerFactory {

  private static final Logger log = LoggerFactory.getLogger(ServletContainerFactory)

  private Map params

  @Override
  public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {
    def jettyConfigurer = new Jetty9Configurer()
    jettyConfigurer.setLogger(log)
    def server = ServerConfigurer.createAndConfigureServer(jettyConfigurer, params) { webapp, context ->
      if(webapp.springBoot) {
        if (isRegisterDefaultServlet())
          addDefaultServlet(context)

        if (isRegisterJspServlet() && ClassUtils.isPresent(getJspServletClassName(), getClass().getClassLoader()))
          addJspServlet(context)

        ServletContextInitializer[] initializersToUse = mergeInitializers(initializers)
        Configuration[] configurations = getWebAppContextConfigurations(context, initializersToUse)
        context.setConfigurations(configurations)
        context.getSessionHandler().getSessionManager().setMaxInactiveInterval(getSessionTimeout())
        postProcessWebAppContext(context)
      }
    }

    for (JettyServerCustomizer customizer : getServerCustomizers())
      customizer.customize(server)

    return getJettyEmbeddedServletContainer(server)
  }

  public void setParams(Map params) {
    this.params = params
  }
}


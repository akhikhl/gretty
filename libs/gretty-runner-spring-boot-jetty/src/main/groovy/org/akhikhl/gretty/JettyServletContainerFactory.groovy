/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

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
class JettyServletContainerFactory extends JettyEmbeddedServletContainerFactory implements GrettyConfigurableServletContainerFactory {

  private Map params

  @Override
  public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {
    def JettyConfigurer = Class.forName('org.akhikhl.gretty.JettyConfigurerImpl', true, this.getClass().classLoader)
    def jettyConfigurer = JettyConfigurer.newInstance()
    jettyConfigurer.setLogger(log)
    def JettyServerConfigurer = Class.forName('org.akhikhl.gretty.JettyServerConfigurer', true, this.getClass().classLoader)
    def server = JettyServerConfigurer.createAndConfigureServer(jettyConfigurer, params) { webapp, context ->
      if(webapp.springBoot) {
        if (isRegisterDefaultServlet())
          addDefaultServlet(context)

        if (isRegisterJspServlet() && ClassUtils.isPresent(getJspServletClassName(), getClass().getClassLoader()))
          addJspServlet(context)

        ServletContextInitializer[] initializersToUse = mergeInitializers(initializers)
        def configurations = getWebAppContextConfigurations(context, initializersToUse)
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

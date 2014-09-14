/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.akhikhl.gretty.JettyServerConfigurer
import org.springframework.boot.context.embedded.EmbeddedServletContainer
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer
import org.springframework.boot.context.embedded.ServletContextInitializer
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@Component('jettyEmbeddedServletContainerFactory')
class JettyServletContainerFactory extends JettyEmbeddedServletContainerFactory implements GrettyConfigurableServletContainerFactory {

  protected static final Logger log = LoggerFactory.getLogger(JettyServletContainerFactory)

  private Map params

  @Override
  public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {
    def jettyConfigurer = Class.forName('org.akhikhl.gretty.JettyConfigurerImpl', true, this.getClass().classLoader).newInstance()
    jettyConfigurer.setLogger(log)
    params.supressSetConfigurations = true
    def server = new JettyServerConfigurer(jettyConfigurer, params).createAndConfigureServer { webapp, context ->

      if(webapp.springBoot) {
        if (isRegisterDefaultServlet())
          addDefaultServlet(context)

        if (isRegisterJspServlet() && ClassUtils.isPresent(getJspServletClassName(), getClass().getClassLoader()))
          addJspServlet(context)
      }
      
      ServletContextInitializer[] initializersToUse = mergeInitializers(initializers)
      def configurations = jettyConfigurer.getConfigurations(webapp)
      BaseResourceConfiguration baseRes = configurations.find { it instanceof BaseResourceConfiguration }
      if(baseRes) {
        baseRes.setExtraResourceBases(webapp.extraResourceBases)
        baseRes.addBaseResourceListener delegate.&configureWithBaseResource.curry(webapp)
      }
      setConfigurations(configurations)
      configurations = getWebAppContextConfigurations(context, initializersToUse)
      context.setConfigurations(configurations)
      context.getSessionHandler().getSessionManager().setMaxInactiveInterval(getSessionTimeout())
      postProcessWebAppContext(context)
    }

    for (JettyServerCustomizer customizer : getServerCustomizers())
      customizer.customize(server)

    return new JettyServletContainer(server, { ->
      if(params.startEvent)
        params.startEvent.onServerStart(new JettyServerStartInfo().getInfo(server, jettyConfigurer, params))
    })
  }

  public void setParams(Map params) {
    this.params = params
  }
}

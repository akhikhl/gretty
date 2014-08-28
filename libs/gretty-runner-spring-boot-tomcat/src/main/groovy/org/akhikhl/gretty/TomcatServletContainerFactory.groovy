/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file 'license.txt' for copying and usage permission.
 */
package org.akhikhl.gretty

import javax.servlet.ServletContainerInitializer
import javax.servlet.ServletContext
import org.akhikhl.gretty.TomcatServerConfigurer
import org.apache.catalina.Context
import org.apache.catalina.Lifecycle
import org.apache.catalina.LifecycleEvent
import org.apache.catalina.LifecycleListener
import org.apache.catalina.Valve
import org.apache.catalina.Wrapper
import org.apache.catalina.connector.Connector
import org.apache.catalina.startup.Tomcat
import org.springframework.boot.context.embedded.EmbeddedServletContainer
import org.springframework.boot.context.embedded.ServletContextInitializer
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedContext
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@Component('tomcatEmbeddedServletContainerFactory')
class TomcatServletContainerFactory extends TomcatEmbeddedServletContainerFactory implements GrettyConfigurableServletContainerFactory {

  protected static final Logger log = LoggerFactory.getLogger(TomcatServletContainerFactory)

  private Map params

  @Override
  public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {

    def tomcatConfigurer = Class.forName('org.akhikhl.gretty.TomcatConfigurerImpl', true, this.getClass().classLoader).newInstance()
    tomcatConfigurer.setLogger(log)

    ServletContextInitializer[] initializersToUse = mergeInitializers(initializers)

    Tomcat tomcat = new TomcatServerConfigurer(tomcatConfigurer, params).createAndConfigureServer { webapp, context ->

      if(webapp.springBoot) {
        if (isRegisterDefaultServlet())
          addDefaultServlet(context)

        if (isRegisterJspServlet() && ClassUtils.isPresent(getJspServletClassName(), getClass().getClassLoader())) {
          addJspServlet(context)
          addJasperInitializer(context)
          context.addLifecycleListener(new StoreMergedWebXmlListener())
        }
      }

      /*
       * Cloning valves is needed to solve topological problem in spring-boot.
       * Scenario:
       * 1) org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat.customizeTomcat adds valves to this factory
       * 2) this closure is being repeatedly invoked for multiple contexts
       * 3) configureContext (below) adds this factory's valves to the given context
       * Result: same valves are added to different contexts, effectively breaking valve chaining.
       * Solution: clone valves, so that each context gets its own set of valves.
       */
      if(!params.suppressCloneContextValves)
        setContextValves(getValves().collect { v ->
          RuntimeUtils.copy v, skipProperties: ['state', 'next']
        })

      configureContext(context, initializersToUse)
    }

    if(params.startEvent)
      tomcat.service.addLifecycleListener(new StartEventListener(tomcat, tomcat.service.findConnectors(), params))

    return getTomcatEmbeddedServletContainer(tomcat)
  }

  public void setParams(Map params) {
    this.params = [:] << params
    this.params.contextClass = TomcatEmbeddedContext
    this.params.parentClassLoader = ClassUtils.getDefaultClassLoader()
  }

	private void addDefaultServlet(Context context) {
		Wrapper defaultServlet = context.createWrapper()
		defaultServlet.setName('default')
		defaultServlet.setServletClass('org.apache.catalina.servlets.DefaultServlet')
		defaultServlet.addInitParameter('debug', '0')
		defaultServlet.addInitParameter('listings', 'false')
		defaultServlet.setLoadOnStartup(1)
		// Otherwise the default location of a Spring DispatcherServlet cannot be set
		defaultServlet.setOverridable(true)
		context.addChild(defaultServlet)
		context.addServletMapping('/', 'default')
	}

	private void addJspServlet(Context context) {
		Wrapper jspServlet = context.createWrapper()
		jspServlet.setName('jsp')
		jspServlet.setServletClass(getJspServletClassName())
		jspServlet.addInitParameter('fork', 'false')
		jspServlet.setLoadOnStartup(3)
		context.addChild(jspServlet)
		context.addServletMapping('*.jsp', 'jsp')
		context.addServletMapping('*.jspx', 'jsp')
	}

	private void addJasperInitializer(TomcatEmbeddedContext context) {
		try {
			ServletContainerInitializer initializer = (ServletContainerInitializer) ClassUtils
					.forName('org.apache.jasper.servlet.JasperInitializer', null)
					.newInstance()
			context.addServletContainerInitializer(initializer, null)
		}
		catch (Exception ex) {
			// Probably not Tomcat 8
		}
	}

	private static class StartEventListener implements LifecycleListener {

    // need to pass connectors separately from tomcat, because TomcatEmbeddedServletContainer
    // nullifies connectors in ctor >> initialize >> removeServiceConnectors

    private final Tomcat tomcat
    private final Connector[] connectors
    private final Map params

    StartEventListener(Tomcat tomcat, Connector[] connectors, Map params) {
      this.tomcat = tomcat
      this.connectors = connectors
      this.params = params
    }

		@Override
		public void lifecycleEvent(LifecycleEvent event) {
			if (event.type == Lifecycle.AFTER_START_EVENT)
        params.startEvent.onServerStart(new TomcatServerStartInfo().getInfo(tomcat, connectors, params))
		}
  }

	private static class StoreMergedWebXmlListener implements LifecycleListener {

		private final String MERGED_WEB_XML = org.apache.tomcat.util.scan.Constants.MERGED_WEB_XML

		@Override
		public void lifecycleEvent(LifecycleEvent event) {
			if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT))
				onStart((Context) event.getLifecycle())
		}

		private void onStart(Context context) {
			ServletContext servletContext = context.getServletContext()
			if (servletContext.getAttribute(this.MERGED_WEB_XML) == null)
				servletContext.setAttribute(this.MERGED_WEB_XML, getEmptyWebXml())
		}

		private String getEmptyWebXml() {
			InputStream stream = TomcatEmbeddedServletContainerFactory.class.getResourceAsStream('empty-web.xml')
			assert stream != null, 'Unable to read empty-web.xml'
      stream.withStream {
        it.getText('UTF-8')
      }
		}
	} // StoreMergedWebXmlListener
}

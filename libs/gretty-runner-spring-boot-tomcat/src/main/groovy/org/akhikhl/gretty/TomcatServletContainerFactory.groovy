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
import org.apache.catalina.Wrapper
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

    def server = new TomcatServerConfigurer().createAndConfigureServer(tomcatConfigurer, params) { webapp, context ->
      if(webapp.springBoot) {
        if (isRegisterDefaultServlet())
          addDefaultServlet(context)

        if (isRegisterJspServlet() && ClassUtils.isPresent(getJspServletClassName(), getClass().getClassLoader())) {
          addJspServlet(context)
          addJasperInitializer(context)
          context.addLifecycleListener(new StoreMergedWebXmlListener())
        }

        ServletContextInitializer[] initializersToUse = mergeInitializers(initializers)
        configureContext(context, initializersToUse)
      }
    }

    return getTomcatEmbeddedServletContainer(server)
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
	private static class StoreMergedWebXmlListener implements LifecycleListener {

		private final String MERGED_WEB_XML = org.apache.tomcat.util.scan.Constants.MERGED_WEB_XML;

		@Override
		public void lifecycleEvent(LifecycleEvent event) {
			if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
				onStart((Context) event.getLifecycle());
			}
		}

		private void onStart(Context context) {
			ServletContext servletContext = context.getServletContext();
			if (servletContext.getAttribute(this.MERGED_WEB_XML) == null) {
				servletContext.setAttribute(this.MERGED_WEB_XML, getEmptyWebXml());
			}
		}

		private String getEmptyWebXml() {
			InputStream stream = TomcatEmbeddedServletContainerFactory.class
					.getResourceAsStream("empty-web.xml");
			Assert.state(stream != null, "Unable to read empty web.xml");
			try {
				try {
					return StreamUtils.copyToString(stream, Charset.forName("UTF-8"));
				}
				finally {
					stream.close();
				}
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

	}
}

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.apache.catalina.Globals
import org.apache.catalina.core.StandardContext
import org.apache.catalina.deploy.WebXml
import org.apache.catalina.startup.ContextConfig
import org.apache.catalina.startup.Tomcat
import org.apache.naming.resources.ProxyDirContext
import org.apache.naming.resources.VirtualDirContext
import org.apache.tomcat.JarScanner
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class TomcatConfigurerImpl implements TomcatConfigurer {

  protected Logger log

  @Override
  void addExtraResourceBases(StandardContext context, List extraResourceBases) {
    if(extraResourceBases) {
      def dir = context.getResources()
      log.warn 'addExtraResourceBases, resources={}, extraResourceBases={}', dir, extraResourceBases
      while(dir instanceof ProxyDirContext)
        dir = dir.getDirContext()
      log.warn 'DirContext={}, docBase={}', dir, dir.getDocBase()
      VirtualDirContext vdc = new VirtualDirContext() {
        protected File file(String name) {
          File result = super.file(name)
          log.warn 'VirtualDirContext.file("{}") -> {}', name, result
          result
        }
      }
      vdc.setDocBase(dir.getDocBase())
      vdc.setExtraResourcePaths extraResourceBases.collect { "/=$it" }.join(',')
      context.setResources(vdc)
    }
  }

  void setResourceBase(StandardContext context, Map webappParams) {
    context.setDocBase(webappParams.resourceBase)
    if(webappParams.extraResourceBases) {
      VirtualDirContext vdc = new VirtualDirContext()
      vdc.setDocBase(webappParams.resourceBase)
      vdc.setExtraResourcePaths(webappParams.extraResourceBases.collect { "/=$it" }.join(','))
      context.setResources(vdc)
    }
  }

  @Override
  ContextConfig createContextConfig(URL[] classpathUrls) {

    new ContextConfig() {

      protected Map<String,WebXml> processJarsForWebFragments(WebXml application) {
        def fragments = super.processJarsForWebFragments(application)
        // here we enable annotation processing for non-jar urls on the classpath
        for(URL url in classpathUrls.findAll { !it.path.endsWith('.jar') && new File(it.path).exists() }) {
          WebXml fragment = new WebXml()
          fragment.setDistributable(true)
          fragment.setURL(url)
          fragment.setName(url.toString())
          fragments[fragment.getName()] = fragment
        }
        fragments
      }
    }
  }

  @Override
  JarScanner createJarScanner(JarScanner jarScanner, JarSkipPatterns skipPatterns) {
    new SkipPatternJarScanner(jarScanner, skipPatterns)
  }

  @Override
  void setBaseDir(Tomcat tomcat, File baseDir) {
    tomcat.baseDir = baseDir.absolutePath
    tomcat.engine.baseDir = baseDir.absolutePath
    System.setProperty(Globals.CATALINA_BASE_PROP, baseDir.absolutePath)
  }

  @Override
  void setLogger(Logger logger) {
    log = logger
  }
}

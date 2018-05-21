/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.apache.catalina.Globals
import org.apache.catalina.Service
import org.apache.catalina.core.StandardContext
import org.apache.catalina.deploy.WebXml
import org.apache.catalina.startup.ContextConfig
import org.apache.catalina.startup.Tomcat
import org.apache.naming.resources.VirtualDirContext
import org.apache.tomcat.JarScanner
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class TomcatConfigurerImpl implements TomcatConfigurer {

  private static final Logger log = LoggerFactory.getLogger(TomcatConfigurerImpl)

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
    new JarScannerEx(skipPatterns)
  }

  @Override
  void setBaseDir(Tomcat tomcat, File baseDir) {
    tomcat.baseDir = baseDir.absolutePath
    tomcat.engine.baseDir = baseDir.absolutePath
    System.setProperty(Globals.CATALINA_BASE_PROP, baseDir.absolutePath)
  }

  @Override
  void setResourceBase(StandardContext context, Map webappParams) {

    context.setDocBase(webappParams.resourceBase)

    List extraResourcePaths = []

    if(webappParams.extraResourceBases)
      extraResourcePaths += webappParams.extraResourceBases.collect { "/=$it" }

    if (webappParams.webXml)
      context.setAltDDName(webappParams.webXml);

    Set<File> classpathJarParentDirs = new LinkedHashSet()

    webappParams.webappClassPath.findAll { it.endsWith('.jar') }.each {
      File jarFile = it.startsWith('file:') ? new File(new URI(it)) : new File(it)
      classpathJarParentDirs.add jarFile
    }

    def webInfLibs = classpathJarParentDirs.toList()

    if(extraResourcePaths || webInfLibs) {
      VirtualDirContext vdc = new VirtualDirContextEx()
      vdc.setExtraResourcePaths(extraResourcePaths.join(','))
      vdc.webInfJars = webInfLibs
      context.setResources(vdc)
    }
  }

  @Override
  void setService(Tomcat tomcat, Service service) {
    tomcat.service = service
  }

  @Override
  void setEngine(Tomcat tomcat, Service service) {
    tomcat.engine = service.getContainer()
  }
}

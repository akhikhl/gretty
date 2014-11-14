/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.apache.catalina.WebResourceRoot
import org.apache.catalina.core.StandardContext
import org.apache.catalina.startup.ContextConfig
import org.apache.catalina.startup.Tomcat
import org.apache.catalina.webresources.DirResourceSet
import org.apache.catalina.webresources.StandardRoot
import org.apache.tomcat.JarScanner
import org.apache.tomcat.util.descriptor.web.WebXml
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class TomcatConfigurerImpl implements TomcatConfigurer {

  protected static boolean isServletApi(String filePath) {
    filePath.matches(/^.*servlet-api.*\.jar$/)
  }

  protected Logger log

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
    tomcat.server.setCatalinaHome(baseDir)
    tomcat.server.setCatalinaBase(baseDir)
  }

  @Override
  void setLogger(Logger logger) {
    log = logger
  }

  @Override
  void setResourceBase(StandardContext context, Map webappParams) {

    context.setDocBase(webappParams.resourceBase)

    WebResourceRoot root = new StandardRoot(context)
    context.setResources(root)

    if(webappParams.extraResourceBases)
      webappParams.extraResourceBases.each { root.createWebResourceSet(WebResourceRoot.ResourceSetType.POST, '/', it, null, '/') }

    Set classpathJarParentDirs = webappParams.webappClassPath.findAll { it.endsWith('.jar') && !isServletApi(it) }.collect({
      File jarFile = it.startsWith('file:') ? new File(new URI(it)) : new File(it)
      jarFile.parentFile.absolutePath
    }) as Set

    classpathJarParentDirs.each {
      root.createWebResourceSet(WebResourceRoot.ResourceSetType.POST, '/WEB-INF/lib', it, null, '/')
    }
  }
}

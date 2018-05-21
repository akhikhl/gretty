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
import org.apache.catalina.WebResourceRoot
import org.apache.catalina.core.StandardContext
import org.apache.catalina.startup.ContextConfig
import org.apache.catalina.startup.Tomcat
import org.apache.catalina.webresources.StandardRoot
import org.apache.tomcat.JarScanner
import org.apache.tomcat.util.descriptor.web.WebXml
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
abstract class AbstractTomcatConfigurerImpl implements TomcatConfigurer {

  private static final Logger log = LoggerFactory.getLogger(AbstractTomcatConfigurerImpl)

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
  void setResourceBase(StandardContext context, Map webappParams) {

    context.setDocBase(webappParams.resourceBase)

    WebResourceRoot root = new StandardRoot(context)
    context.setResources(root)

    if(webappParams.extraResourceBases)
      webappParams.extraResourceBases.each { root.createWebResourceSet(WebResourceRoot.ResourceSetType.POST, '/', it, null, '/') }


    if (webappParams.webXml)
      context.setAltDDName(webappParams.webXml);

    Set classpathJarParentDirs = webappParams.webappClassPath.findAll { it.endsWith('.jar') }.collect({
      File jarFile = it.startsWith('file:') ? new File(new URI(it)) : new File(it)
      jarFile
    }) as Set

    classpathJarParentDirs.each { File it ->
      root.createWebResourceSet(WebResourceRoot.ResourceSetType.POST, '/WEB-INF/lib/' + it.name, it.absolutePath, null, '/')
    }
  }
}

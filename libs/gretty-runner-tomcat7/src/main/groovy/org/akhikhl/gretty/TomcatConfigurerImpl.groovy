/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.apache.catalina.deploy.WebXml
import org.apache.catalina.startup.ContextConfig
import org.apache.tomcat.JarScanner
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class TomcatConfigurerImpl implements TomcatConfigurer {

  protected Logger log

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

  JarScanner createJarScanner(JarScanner jarScanner, JarSkipPatterns skipPatterns) {
    new SkipPatternJarScanner(jarScanner, skipPatterns)
  }

  void setLogger(Logger logger) {
    log = logger
  }
}

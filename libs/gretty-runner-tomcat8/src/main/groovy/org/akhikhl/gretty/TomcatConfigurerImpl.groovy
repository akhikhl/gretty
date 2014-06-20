/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.apache.catalina.startup.ContextConfig
import org.apache.tomcat.JarScanner
import org.apache.tomcat.util.descriptor.web.WebXml
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

      protected void processAnnotationsFile(File file, WebXml fragment, boolean handlesTypesOnly) {
        log.debug 'processAnnotationsFile file={}', file
        super.processAnnotationsFile(file, fragment, handlesTypesOnly)
      }

      protected void processAnnotationsUrl(URL url, WebXml fragment, boolean handlesTypesOnly) {
        log.debug 'processAnnotationsUrl url={}', url
        super.processAnnotationsUrl(url, fragment, handlesTypesOnly)
      }

      protected Map<String,WebXml> processJarsForWebFragments(WebXml application) {
        def fragments = super.processJarsForWebFragments(application)
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

  JarScanner createJarScanner(JarScanner jarScanner, String skipPattern) {
    new SkipPatternJarScanner(jarScanner, skipPattern)
  }

  void setLogger(Logger logger) {
    log = logger
  }
}



/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.util.resource.FileResource
import org.eclipse.jetty.util.resource.JarResource
import org.eclipse.jetty.util.resource.Resource

/**
 * This servlet emulates webjars for servlet-api older than 3.0.
 *
 * @author akhikhl
 */
class DefaultServletEx extends DefaultServlet {
  
  Resource getResource(String pathInContext) {
    def result = super.getResource(pathInContext)
    if(result instanceof FileResource && !result.getFile()?.exists()) {
      if(pathInContext.startsWith('/webjars/')) {
        String webjarsPath = 'META-INF/resources' + pathInContext
        URL resourceURL = Thread.currentThread().getContextClassLoader().getResource(webjarsPath)
        if(resourceURL)
          result = new JarResource(resourceURL)
      }
    }
    result
  }
}

/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.apache.catalina.Service
import org.apache.catalina.core.StandardContext
import org.apache.catalina.startup.ContextConfig
import org.apache.catalina.startup.Tomcat
import org.apache.tomcat.JarScanner
import org.slf4j.Logger

/**
 *
 * @author akhikhl
 */
interface TomcatConfigurer {

  ContextConfig createContextConfig(URL[] classpathUrls)

  JarScanner createJarScanner(JarScanner jarScanner, JarSkipPatterns skipPatterns)

  void setBaseDir(Tomcat tomcat, File baseDir)

  void setResourceBase(StandardContext context, Map webappParams)

  void setService(Tomcat tomcat, Service service)

  void setEngine(Tomcat tomcat, Service service)
}

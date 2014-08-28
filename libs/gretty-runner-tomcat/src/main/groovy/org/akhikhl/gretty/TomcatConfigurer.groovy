/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

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

  void addExtraResourceBases(StandardContext context, List extraResourceBases)

  ContextConfig createContextConfig(URL[] classpathUrls)

  JarScanner createJarScanner(JarScanner jarScanner, JarSkipPatterns skipPatterns)

  void setBaseDir(Tomcat tomcat, File baseDir)

  void setLogger(Logger logger)
}


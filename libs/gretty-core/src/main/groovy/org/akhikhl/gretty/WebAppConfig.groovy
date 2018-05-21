/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.ToString
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.apache.commons.io.FilenameUtils

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
@ToString
class WebAppConfig {

  def contextPath
  def initParameters
  def realm
  def realmConfigFile
  def contextConfigFile
  def scanDirs
  /**
   * Specifies if Gretty should automatically add dependencyProjects' sourceSets to scanDirs
   */
  Boolean scanDependencies
  def fastReload
  Boolean recompileOnSourceChange
  Boolean reloadOnClassChange
  Boolean reloadOnConfigChange
  Boolean reloadOnLibChange

  def resourceBase
  List extraResourceBases

  Set<String> beforeClassPath
  Set<String> classPath
  String webInfIncludeJarPattern

  String projectPath
  Boolean inplace
  /*
   * Specifies mode for inplace feature: "hard" for directly serving src/main/webapp folder and "soft" for preparing build/inplaceWebapp first
   */
  String inplaceMode

  /*
   * Specifies a custom location for the web.xml file
   */
  String webXml

  Boolean springBoot
  String springBootMainClass

  private static void addClassPathEntries(Set<String> classPath, Object... args) {
    for(def arg in args) {
      if(arg != null) {
        String url
        if (arg instanceof URL)
          url = ((URL)arg).toString()
        else if (arg instanceof File)
          url = ((File)arg).getPath()
        else
          url = arg.toString()
        classPath.add(url)
      }
    }
  }

  void beforeClassPath(Object... args) {
    if(args) {
      addClassPathEntries(beforeClassPath ?: (beforeClassPath = new LinkedHashSet<>()), args)
    }
  }

  void classPath(Object... args) {
    if(args) {
      addClassPathEntries(classPath ?: (classPath = new LinkedHashSet<>()), args)
    }
  }

  void extraResourceBase(arg) {
    if(extraResourceBases == null)
      extraResourceBases = []
    extraResourceBases.add(arg)
  }

  void extraResourceBases(Object... args) {
    for(def arg in args) {
      if(arg != null) {
        if(extraResourceBases == null)
          extraResourceBases = []
        extraResourceBases.add(arg)
      }
    }
  }

  void fastReload(String arg) {
    if(fastReload == null)
      fastReload = []
    fastReload.add(arg)
  }

  void fastReload(File arg) {
    if(fastReload == null)
      fastReload = []
    fastReload.add(arg)
  }

  void fastReload(Map map) {
    if(fastReload == null)
      fastReload = []
    fastReload.add(map)
  }

  static WebAppConfig getDefaultWebAppConfigForWarFile(File warFile) {
    WebAppConfig result = new WebAppConfig()
    String baseName = FilenameUtils.getBaseName(warFile.name)
    // remove version suffix
    baseName = baseName.replaceAll(/([\da-zA-Z_.-]+?)-((\d+\.)+[\da-zA-Z_.-]*)/, '$1')
    result.contextPath = '/' + baseName
    result.resourceBase = warFile.absolutePath
    return result
  }

  // use contextConfigFile instead
  @Deprecated
  def getJettyEnvXmlFile() {
    contextConfigFile
  }

  void initParameter(key, value) {
    if(initParameters == null)
      initParameters = [:]
    initParameters[key] = value
  }

  protected void prepareToRun() {
    ConfigUtils.resolveClosures(this)
    if(contextPath instanceof String) {
      if(!contextPath.startsWith('/'))
        contextPath = '/' + contextPath
      if(contextPath != '/' && contextPath.endsWith('/'))
        contextPath = contextPath.substring(0, contextPath.length() - 1)
    }
    ConfigUtils.resolveClosures(initParameters)
  }

  void scanDir(String value) {
    if(scanDirs == null)
      scanDirs = []
    scanDirs.add(new File(value))
  }

  void scanDir(File value) {
    if(scanDirs == null)
      scanDirs = []
    scanDirs.add(value)
  }

  void scanDir(Object[] args) {
    for(def arg in args)
      if(arg != null) {
        if(scanDirs == null)
          scanDirs = []
        scanDirs.add(arg)
      }
  }

  void setFastReload(newValue) {
    if(newValue == null || Collection.class.isAssignableFrom(newValue.getClass()))
      fastReload = newValue
    else
      fastReload = [ newValue ]
  }

  // use contextConfigFile instead
  @Deprecated
  void setJettyEnvXmlFile(newValue) {
    contextConfigFile = newValue
  }
}

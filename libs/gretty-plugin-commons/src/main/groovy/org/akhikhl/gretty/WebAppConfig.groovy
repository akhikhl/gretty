/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class WebAppConfig {

  String contextPath
  Map initParameters
  String realm
  def realmConfigFile
  def jettyEnvXmlFile
  List scanDirs
  def fastReload
  String inplaceResourceBase
  String warResourceBase
  Collection<URL> classPath

  String projectPath
  Boolean inplace

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

  protected static WebAppConfig getDefault(Project project) {
    WebAppConfig result = new WebAppConfig()
    result.contextPath = '/' + project.name
    result.jettyEnvXmlFile = 'jetty-env.xml'
    result.inplaceResourceBase = "${project.buildDir}/inplaceWebapp" as String
    result.warResourceBase = ProjectUtils.getFinalWarPath(project).toString()
    result.projectPath = project.path
    result.inplace = true
    return result
  }

  void initParameter(key, value) {
    if(initParameters == null)
      initParameters = [:]
    initParameters[key] = value
  }

  protected void resolve(Project project) {
    if(contextPath != null && !contextPath.startsWith('/'))
      contextPath = '/' + contextPath
    if(initParameters) {
      def initParams = [:]
      for(def e in initParameters) {
        def paramValue = e.value
        if(paramValue instanceof Closure)
          paramValue = paramValue()
        initParams[e.key] = paramValue
      }
      initParameters = initParams
    }
    realmConfigFile = ProjectUtils.resolveSingleFile(project, realmConfigFile)
    jettyEnvXmlFile = ProjectUtils.resolveSingleFile(project, jettyEnvXmlFile)
    classPath = ProjectUtils.getClassPath(project, inplace)
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

  void setFastReload(boolean newValue) {
    fastReload = [ newValue ]
  }
}

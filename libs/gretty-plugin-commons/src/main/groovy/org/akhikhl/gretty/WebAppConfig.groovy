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

  boolean inplace = true
  Collection<URL> classPath
  String contextPath
  Map initParameters
  String realm
  def realmConfigFile
  def jettyEnvXmlFile
  List scanDirs // list of additional scan directories
  def fastReload
  String inplaceResourceBase
  String warResourceBase

  protected RealmInfo realmInfo

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

  void initParameter(key, value) {
    if(initParameters == null)
      initParameters = [:]
    initParameters[key] = value
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

  protected void setupProperties(Project project) {
    if(classPath == null) classPath = ProjectUtils.getClassPath(project, inplace)
    if(contextPath == null) {
      contextPath = project.gretty.contextPath
      if(contextPath == null)
        contextPath = '/' + project.name
      else if(!contextPath.startsWith('/'))
        contextPath = '/' + contextPath
    }
    if(initParameters == null) initParameters = ProjectUtils.getInitParameters(project)
    if(!realm || !realmConfigFile)
      realmInfo = ProjectUtils.getRealmInfo(project)
    else {
      if(!(realmConfigFile instanceof File))
        realmConfigFile = new File(realmConfigFile)
      if(!realmConfigFile.isAbsolute())
        realmConfigFile = new File(project.webAppDir, realmConfigFile.path)
      realmInfo = new RealmInfo(realm: realm, realmConfigFile: realmConfigFile.absolutePath)
    }
    if(jettyEnvXmlFile == null)
      jettyEnvXmlFile = ProjectUtils.getJettyEnvXmlFile(project)
    else
      jettyEnvXmlFile = ProjectUtils.resolveSingleFile(project, jettyEnvXmlFile)
    if(scanDirs == null) scanDirs = project.gretty.scanDirs
    if(inplaceResourceBase == null) inplaceResourceBase = "${project.buildDir}/inplaceWebapp"
    if(warResourceBase == null) warResourceBase = ProjectUtils.getFinalWarPath(project).toString()
  }
}

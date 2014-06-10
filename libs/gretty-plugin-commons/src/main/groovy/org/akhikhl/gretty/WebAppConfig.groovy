/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class WebAppConfig {

  def contextPath
  def initParameters
  def realm
  def realmConfigFile
  def jettyEnvXmlFile
  def scanDirs
  def fastReload
  def inplaceResourceBase
  def warResourceBase

  Set<URL> classPath

  String projectPath
  Boolean inplace

  def springBootSources

  void classPath(Object... args) {
    if(args) {
      if(classPath == null)
        classPath = new LinkedHashSet<URL>()
      for(def arg in args) {
        if(arg != null)
          classPath.add(arg)
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

  protected static WebAppConfig getDefaultForProject(Project project) {
    WebAppConfig result = new WebAppConfig()
    result.contextPath = '/' + project.name
    result.realmConfigFile = 'jetty-realm.properties'
    result.jettyEnvXmlFile = 'jetty-env.xml'
    result.inplaceResourceBase = "${project.buildDir}/inplaceWebapp" as String
    result.warResourceBase = ProjectUtils.getFinalArchivePath(project).toString()
    result.projectPath = project.path
    return result
  }

  protected static WebAppConfig getDefaultForMavenDependency(Project project, String dependency) {
    WebAppConfig result = new WebAppConfig()
    result.contextPath = '/' + dependency.split(':')[1]
    result.warResourceBase = {
      def gav = dependency.split(':')
      def artifact = project.configurations.farm.resolvedConfiguration.resolvedArtifacts.find { it.moduleVersion.id.group == gav[0] && it.moduleVersion.id.name == gav[1] }
      artifact.file.absolutePath
    }
    result.inplace = false
    return result
  }

  protected static WebAppConfig getDefaultForWarFile(Project project, File warFile) {
    WebAppConfig result = new WebAppConfig()
    String baseName = FilenameUtils.getBaseName(warFile.name)
    // remove version suffix
    baseName = baseName.replaceAll(/([\da-zA-Z_.-]+?)-((\d+\.)+[\da-zA-Z_.-]*)/, '$1')
    result.contextPath = '/' + baseName
    result.warResourceBase = warFile.absolutePath
    result.inplace = false
    return result
  }

  void initParameter(key, value) {
    if(initParameters == null)
      initParameters = [:]
    initParameters[key] = value
  }

  protected void prepareToRun() {
    ConfigUtils.resolveClosures(this)
    if(contextPath instanceof String && !contextPath.startsWith('/'))
      contextPath = '/' + contextPath
    ConfigUtils.resolveClosures(initParameters)
  }

  protected void resolve(Project project) {
    realmConfigFile = ProjectUtils.resolveSingleFile(project, realmConfigFile)
    jettyEnvXmlFile = ProjectUtils.resolveSingleFile(project, jettyEnvXmlFile)
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

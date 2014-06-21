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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class WebAppConfig {

  private static final Logger log = LoggerFactory.getLogger(WebAppConfig)

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

  Boolean springBoot
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
    result.realmConfigFile = null
    result.jettyEnvXmlFile = 'jetty-env.xml'
    result.fastReload = true
    result.inplaceResourceBase = "${project.buildDir}/inplaceWebapp/" as String
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
    if(contextPath instanceof String) {
      if(!contextPath.startsWith('/'))
        contextPath = '/' + contextPath
      if(contextPath != '/' && contextPath.endsWith('/'))
        contextPath = contextPath.substring(0, contextPath.length() - 1)
    }
    ConfigUtils.resolveClosures(initParameters)
  }

  protected void resolve(Project project, String servletContainer) {
    
    String servletContainerType = ServletContainerConfig.getConfig(servletContainer).servletContainerType
    
    boolean defaultRealmConfigFile = false
    if(!realmConfigFile) {
      defaultRealmConfigFile = true
      if(servletContainerType == 'tomcat')
        realmConfigFile = 'tomcat-users.xml'
      else
        realmConfigFile = 'jetty-realm.properties'
    }
    
    if(servletContainerType == 'jetty' && !defaultRealmConfigFile && !realm)
      log.warn 'Realm config file is specified, but realm is not specified.'
    
    def resolvedRealmConfigFile = ProjectUtils.resolveSingleFile(project, realmConfigFile)
    if(!resolvedRealmConfigFile && !defaultRealmConfigFile)
      log.warn 'The realm config file {} does not exist.', realmConfigFile
    realmConfigFile = resolvedRealmConfigFile
    
    if(servletContainerType == 'jetty')
      jettyEnvXmlFile = ProjectUtils.resolveSingleFile(project, jettyEnvXmlFile)
    else
      jettyEnvXmlFile = null
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
}

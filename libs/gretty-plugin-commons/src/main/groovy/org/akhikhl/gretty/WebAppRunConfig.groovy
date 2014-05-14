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
class WebAppRunConfig extends WebAppConfig {

  boolean inplace = true

  protected void setupProperties(Project project, WebAppConfig sourceConfig) {
    if(contextPath == null) {
      contextPath = sourceConfig.contextPath
      if(contextPath == null)
        contextPath = '/' + project.name
      else if(!contextPath.startsWith('/'))
        contextPath = '/' + contextPath
    }
    if(initParameters == null) initParameters = sourceConfig.initParameters
    def initParams = [:]
    for(def e in initParameters) {
      def paramValue = e.value
      if(paramValue instanceof Closure)
        paramValue = paramValue()
      initParams[e.key] = paramValue
    }
    initParameters = initParams
    if(realm == null) realm = sourceConfig.realm
    if(realmConfigFile == null) realmConfigFile = sourceConfig.realmConfigFile
    realmConfigFile = ProjectUtils.resolveSingleFile(project, realmConfigFile)
    if(jettyEnvXmlFile == null) jettyEnvXmlFile = sourceConfig.jettyEnvXmlFile ?: 'jetty-env.xml'
    jettyEnvXmlFile = ProjectUtils.resolveSingleFile(project, jettyEnvXmlFile)
    if(scanDirs == null) scanDirs = sourceConfig.scanDirs
    if(fastReload == null) fastReload = sourceConfig.fastReload
    if(inplaceResourceBase == null) inplaceResourceBase = sourceConfig.inplaceResourceBase ?: "${project.buildDir}/inplaceWebapp"
    if(warResourceBase == null) warResourceBase = sourceConfig.warResourceBase ?: ProjectUtils.getFinalWarPath(project).toString()
    if(classPath == null) classPath = sourceConfig.classPath ?: ProjectUtils.getClassPath(project, inplace)
  }
}


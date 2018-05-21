/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class FarmConfigurer {

  private static final Logger log = LoggerFactory.getLogger(FarmConfigurer)

  private final Project project
  private ServerConfig sconfig

  FarmConfigurer(Project project) {
    this.project = project
  }

  void configureFarm(FarmExtension dstFarm, FarmExtension[] srcFarms = []) {
    srcFarms = srcFarms.findAll()
    ConfigUtils.complementProperties(dstFarm.serverConfig, srcFarms*.serverConfig + [ project.gretty.serverConfig, ProjectUtils.getDefaultServerConfig(project) ])
    sconfig = dstFarm.serverConfig
    ProjectUtils.resolveServerConfig(project, dstFarm.serverConfig)
    for(def f in srcFarms) {
      mergeWebAppRefMaps(dstFarm.webAppRefs_, f.webAppRefs)
      dstFarm.integrationTestProjects_.addAll(f.integrationTestProjects)
      dstFarm.includes_.addAll(f.includes)
    }
    if(!dstFarm.webAppRefs && !dstFarm.includes)
      dstFarm.webAppRefs = getDefaultWebAppRefMap()
    if(dstFarm.integrationTestTask == null)
      dstFarm.integrationTestTask = srcFarms.findResult { it.integrationTestTask }
  }

  FarmExtension findProjectFarm(String sourceFarmName) {
    project.farms.farmsMap[sourceFarmName]
  }

  Map getDefaultWebAppRefMap() {
    Map result = [:]
    project.subprojects.findAll { it.extensions.findByName('gretty') }.each { p ->
      result[p.path] = [:]
    }
    if(!result && project.extensions.findByName('gretty'))
      result[project.path] = [:]
    result
  }

  Iterable<Project> getIntegrationTestProjects(List integrationTestProjects) {
    integrationTestProjects.findResults { projectRef ->
      FarmConfigurerUtil.resolveProjectRefToProject(project, projectRef)
    }
  }

  FarmExtension getProjectFarm(String sourceFarmName) {
    def sourceFarm = project.farms.farmsMap[sourceFarmName]
    if(!sourceFarm)
      throw new GradleException("Farm '${sourceFarmName}' is not defined in project farms")
    sourceFarm
  }

  WebAppConfig getWebAppConfigForMavenDependency(Map options, String dependency, String farmName) {
    WebAppConfig wconfig = new WebAppConfig()
    ConfigUtils.complementProperties(wconfig, options, ProjectUtils.getDefaultWebAppConfigForMavenDependency(project, farmName,  dependency, options.dependencies))
    wconfig.inplace = false // always war-file, ignore options.inplace
    ProjectUtils.resolveWebAppConfig(null, wconfig, sconfig)
    wconfig
  }

  WebAppConfig getWebAppConfigForProject(Map options, Project proj, Boolean inplace = null, String inplaceMode = null) {
    WebAppConfig wconfig = new WebAppConfig()
    if(!proj.extensions.findByName('gretty'))
      throw new GradleException("${proj} does not contain gretty extension. Please make sure that gretty plugin is applied to it.")
    ConfigUtils.complementProperties(wconfig, options, proj.gretty.webAppConfig, ProjectUtils.getDefaultWebAppConfigForProject(proj), new WebAppConfig(inplace: inplace, inplaceMode: inplaceMode))
    ProjectUtils.resolveWebAppConfig(proj, wconfig, sconfig)
    wconfig
  }

  WebAppConfig getWebAppConfigForWarFile(Map options, File warFile) {
    WebAppConfig wconfig = new WebAppConfig()
    ConfigUtils.complementProperties(wconfig, options, WebAppConfig.getDefaultWebAppConfigForWarFile(warFile))
    wconfig.inplace = false // always war-file, ignore options.inplace
    ProjectUtils.resolveWebAppConfig(null, wconfig, sconfig)
    wconfig
  }

  Iterable<WebAppConfig> getWebAppConfigsForProjects(Map wrefs, Boolean inplace = null, String inplaceMode = null) {
    wrefs.findResults { wref, options ->
      if(options.inplace != null)
        inplace = options.inplace
      def proj = FarmConfigurerUtil.resolveProjectRefToProject(project, wref)
      proj ? getWebAppConfigForProject(options, proj, inplace, inplaceMode) : null
    }
  }

  Iterable<Project> getWebAppProjects(Map wrefs) {
    wrefs.keySet().findResults { wref ->
      FarmConfigurerUtil.resolveProjectRefToProject(project, wref)
    }
  }

  static void mergeWebAppRefMaps(Map dst, Map src) {
    src.each { webAppRef, options ->
      def existingOptions = dst[webAppRef]
      if(existingOptions == null)
        existingOptions = dst[webAppRef] = [:]
      existingOptions << options
    }
  }

  // attention: this method may modify project configurations and dependencies.
  void resolveWebAppRefs(String farmName, Map wrefs, Collection<WebAppConfig> destWebAppConfigs, Boolean inplace = null, String inplaceMode = null) {
    wrefs.each { wref, options ->
      if(options.inplace != null)
        inplace = options.inplace
      def proj, warFile

      // little hack for overlays:
      if(options.finalArchivePath) {
        wref = options.finalArchivePath
      }

      def typeAndResult = FarmConfigurerUtil.resolveWebAppType(project, options.suppressMavenToProjectResolution, wref)
      def type = typeAndResult[0]
      def result = typeAndResult[1]
      switch (type) {
        case FarmWebappType.PROJECT:
          proj = result
          break
        case FarmWebappType.WAR_FILE:
          warFile = result
          break
        case FarmWebappType.DEPENDENCY_TO_PROJECT:
          proj = result
          log.info '{} comes from project {}, so using project instead of maven dependency', wref, proj.path
          break
        case FarmWebappType.WAR_DEPENDENCY:
          log.info '{} is not an existing project or war-file, treating it as a maven dependency', wref
          break
      }

      WebAppConfig webappConfig
      if(proj)
        webappConfig = getWebAppConfigForProject(options, proj, inplace, inplaceMode)
      else if (warFile)
        webappConfig = getWebAppConfigForWarFile(options, warFile)
      else {
        webappConfig = getWebAppConfigForMavenDependency(options, wref, farmName)
      }
      destWebAppConfigs.add(webappConfig)
    }
  }

  /**
   *
   * @param webAppRef
   * @return
   * @deprecated use {@link FarmConfigurerUtil} instead
   */
  @Deprecated
  Project resolveWebAppRefToProject(webAppRef) {
    FarmConfigurerUtil.resolveProjectRefToProject(project, webAppRef)
  }

  /**
   *
   * @param webAppRef
   * @return
   * @deprecated use {@link FarmConfigurerUtil} instead
   */
  @Deprecated
  File resolveWebAppRefToWarFile(webAppRef) {
    FarmConfigurerUtil.resolveWebAppRefToWarFile(project, webAppRef)
  }
}

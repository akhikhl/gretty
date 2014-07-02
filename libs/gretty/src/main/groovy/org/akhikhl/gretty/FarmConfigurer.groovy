/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class FarmConfigurer {

  private static final Logger log = LoggerFactory.getLogger(FarmConfigurer)

  private final Project project
  private String servletContainer

  FarmConfigurer(Project project) {
    this.project = project
  }

  void configureFarm(FarmExtension dstFarm, FarmExtension[] srcFarms = []) {
    srcFarms = srcFarms.findAll()
    ConfigUtils.complementProperties(dstFarm.serverConfig, srcFarms*.serverConfig + [ ProjectUtils.getDefaultServerConfig(project) ])
    servletContainer = dstFarm.serverConfig.servletContainer
    ProjectUtils.resolveServerConfig(project, dstFarm.serverConfig)
    for(def f in srcFarms)
      mergeWebAppRefMaps(dstFarm.webAppRefs, f.webAppRefs)
    if(!dstFarm.webAppRefs)
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

  FarmExtension getProjectFarm(String sourceFarmName) {
    def sourceFarm = project.farms.farmsMap[sourceFarmName]
    if(!sourceFarm)
      throw new GradleException("Farm '${sourceFarmName}' is not defined in project farms")
    sourceFarm
  }

  WebAppConfig getWebAppConfigForMavenDependency(Map options, String dependency) {
    WebAppConfig wconfig = new WebAppConfig()
    ConfigUtils.complementProperties(wconfig, options, ProjectUtils.getDefaultWebAppConfigForMavenDependency(project, dependency))
    wconfig.inplace = false // always war-file, ignore options.inplace
    ProjectUtils.resolveWebAppConfig(null, wconfig, servletContainer)
    wconfig
  }

  WebAppConfig getWebAppConfigForProject(Map options, Project proj, Boolean inplace = null) {
    WebAppConfig wconfig = new WebAppConfig()
    if(!proj.extensions.findByName('gretty'))
      throw new GradleException("${proj} does not contain gretty extension. Please make sure that gretty plugin is applied to it.")
    ConfigUtils.complementProperties(wconfig, options, proj.gretty.webAppConfig, ProjectUtils.getDefaultWebAppConfigForProject(proj), new WebAppConfig(inplace: inplace))
    ProjectUtils.resolveWebAppConfig(proj, wconfig, servletContainer)
    wconfig
  }

  WebAppConfig getWebAppConfigForWarFile(Map options, File warFile) {
    WebAppConfig wconfig = new WebAppConfig()
    ConfigUtils.complementProperties(wconfig, options, WebAppConfig.getDefaultWebAppConfigForWarFile(warFile))
    wconfig.inplace = false // always war-file, ignore options.inplace
    ProjectUtils.resolveWebAppConfig(null, wconfig, servletContainer)
    wconfig
  }

  Iterable<WebAppConfig> getWebAppConfigsForProjects(Map wrefs, Boolean inplace = null) {
    wrefs.findResults { wref, options ->
      if(options.inplace != null)
        inplace = options.inplace
      def proj = resolveWebAppRefToProject(wref)
      proj ? getWebAppConfigForProject(options, proj, inplace) : null
    }
  }

  Iterable<Project> getWebAppProjects(Map wrefs) {
    wrefs.findResults { wref, options ->
      resolveWebAppRefToProject(wref)
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
  void resolveWebAppRefs(Map wrefs, Collection<WebAppConfig> destWebAppConfigs, Boolean inplace = null) {
    wrefs.each { wref, options ->
      if(options.inplace != null)
        inplace = options.inplace
      def proj = resolveWebAppRefToProject(wref)
      def warFile
      if(!proj) {
        warFile = resolveWebAppRefToWarFile(wref)
        if(!warFile) {
          wref = wref.toString()
          def gav = wref.split(':')
          if(gav.length != 3)
            throw new GradleException("'${wref}' is not an existing project or file or maven dependency.")
          log.warn '{} is not an existing project or war-file, treating it as a maven dependency', wref
          if(!options.suppressMavenToProjectResolution) {
            proj = project.rootProject.allprojects.find { it.group == gav[0] && it.name == gav[1] }
            if(proj)
              log.warn '{} comes from project {}, so using project instead of maven dependency', wref, proj.path
          }
        }
      }
      WebAppConfig webappConfig
      if(proj)
        webappConfig = getWebAppConfigForProject(options, proj, inplace)
      else if (warFile)
        webappConfig = getWebAppConfigForWarFile(options, warFile)
      else {
        project.configurations.maybeCreate('farm')
        project.dependencies.add 'farm', wref
        webappConfig = getWebAppConfigForMavenDependency(options, wref)
      }
      destWebAppConfigs.add(webappConfig)
    }
  }

  Project resolveWebAppRefToProject(webAppRef) {
    def proj
    if(webAppRef instanceof Project)
      proj = webAppRef
    else if(webAppRef instanceof String || webAppRef instanceof GString)
      proj = project.findProject(webAppRef)
    proj
  }

  File resolveWebAppRefToWarFile(webAppRef) {
    File warFile = webAppRef instanceof File ? webAppRef : new File(webAppRef.toString())
    if(!warFile.isFile() && !warFile.isAbsolute())
      warFile = new File(project.projectDir, warFile.path)
    warFile.isFile() ? warFile.absoluteFile : null
  }
}

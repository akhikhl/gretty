/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.regex.Pattern
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class ProjectUtils {

  static class RealmInfo {
    String realm
    String realmConfigFile
  }

  private static final Logger log = LoggerFactory.getLogger(ProjectUtils)

  private static Set collectOverlayJars(Project project) {
    Set overlayJars = new HashSet()
    def addOverlayJars // separate declaration from init to enable recursion
    addOverlayJars = { Project proj ->
      if(proj.extensions.findByName('gretty'))
        for(def overlay in proj.gretty.overlays) {
          overlay = proj.project(overlay)
          File archivePath = overlay.tasks.findByName('jar')?.archivePath
          if(archivePath)
            overlayJars.add(archivePath)
          addOverlayJars(overlay) // recursion
        }
    }
    addOverlayJars(project)
    return overlayJars
  }

  static boolean findFileInClassPath(Project project, Pattern filePattern) {
    Set overlayJars = collectOverlayJars(project)
    def findIt
    findIt = { Project proj ->
      if(proj.sourceSets.main.output.files.find { dir ->
        boolean result = dir.listFiles().find { it.path =~ filePattern }
        log.debug 'findFileInClassPath dir: {}, result: {}', dir, result
        result
      })
        return true
      if(proj.configurations.runtime.files.findAll { !overlayJars.contains(it) }.find({ jarFile ->
        boolean result = project.zipTree(jarFile).files.find { it.path =~ filePattern }
        log.debug 'findFileInClassPath jar: {}, result: {}', jarFile, result
        result
      }))
        return true
      if(proj.extensions.findByName('gretty'))
        for(String overlay in proj.gretty.overlays.reverse())
          if(findIt(proj.project(overlay)))
            return true
    }
    findIt(project)
  }

  static String getContextPath(Project project) {
    String contextPath = project.gretty.contextPath
    if(!contextPath)
      for(def overlay in project.gretty.overlays.reverse()) {
        overlay = project.project(overlay)
        if(overlay.extensions.findByName('gretty')) {
          if(overlay.gretty.contextPath) {
            contextPath = overlay.gretty.contextPath
            break
          }
        } else
          log.warn 'Project {} is not gretty-enabled, could not extract it\'s context path', overlay
      }
    contextPath = contextPath ?: "/${project.name}"
    return contextPath
  }

  static Set<URL> getClassPath(Project project, boolean inplace) {
    Set<URL> urls = new LinkedHashSet()
    if(inplace) {
      def addProjectClassPath
      addProjectClassPath = { Project proj ->
        urls.addAll proj.sourceSets.main.output.files.collect { it.toURI().toURL() }
        urls.addAll proj.configurations.runtime.files.collect { it.toURI().toURL() }
        // ATTENTION: order of overlay classpath is important!
        if(proj.extensions.findByName('gretty'))
          for(String overlay in proj.gretty.overlays.reverse())
            addProjectClassPath(proj.project(overlay))
      }
      addProjectClassPath(project)
      for(File overlayJar in collectOverlayJars(project))
        if(urls.remove(overlayJar.toURI().toURL()))
          log.debug '{} is overlay jar, exclude from classpath', overlayJar
      for(File grettyConfigJar in project.configurations.grettyHelperConfig.files)
        if(urls.remove(grettyConfigJar.toURI().toURL()))
          log.debug '{} is gretty-config jar, exclude from classpath', grettyConfigJar
    }
    for(URL url in urls)
      log.debug 'classpath URL: {}', url
    return urls
  }

  static File getFinalWarPath(Project project) {
    project.ext.properties.containsKey('finalWarPath') ? project.ext.finalWarPath : project.tasks.war.archivePath
  }

  static Map getInitParameters(Project project) {
    Map initParams = [:]
    for(def overlay in project.gretty.overlays) {
      overlay = project.project(overlay)
      if(overlay.extensions.findByName('gretty')) {
        for(def e in overlay.gretty.initParameters) {
          def paramValue = e.value
          if(paramValue instanceof Closure)
            paramValue = paramValue()
          initParams[e.key] = paramValue
        }
      } else
        log.warn 'Project {} is not gretty-enabled, could not extract it\'s init parameters', overlay
    }
    for(def e in project.gretty.initParameters) {
      def paramValue = e.value
      if(paramValue instanceof Closure)
        paramValue = paramValue()
      initParams[e.key] = paramValue
    }
    return initParams
  }

  static RealmInfo getRealmInfo(Project project) {
    String realm = project.gretty.realm
    String realmConfigFile = project.gretty.realmConfigFile
    if(realmConfigFile && !new File(realmConfigFile).isAbsolute())
      realmConfigFile = "${project.webAppDir.absolutePath}/${realmConfigFile}"
    if(!realm || !realmConfigFile)
      for(def overlay in project.gretty.overlays.reverse()) {
        overlay = project.project(overlay)
        if(overlay.extensions.findByName('gretty')) {
          if(overlay.gretty.realm && overlay.gretty.realmConfigFile) {
            realm = overlay.gretty.realm
            realmConfigFile = overlay.gretty.realmConfigFile
            if(realmConfigFile && !new File(realmConfigFile).isAbsolute())
              realmConfigFile = "${overlay.webAppDir.absolutePath}/${realmConfigFile}"
            break
          }
        } else
          log.warn 'Project {} is not gretty-enabled, could not extract it\'s realm', overlay
      }
    return new RealmInfo(realm: realm, realmConfigFile: realmConfigFile)
  }

  static void prepareInplaceWebAppFolder(Project project) {
    // ATTENTION: overlay copy order is important!
    for(String overlay in project.gretty.overlays)
      project.copy {
        from project.project(overlay).webAppDir
        into "${project.buildDir}/inplaceWebapp"
      }
    project.copy {
      from project.webAppDir
      into "${project.buildDir}/inplaceWebapp"
    }
  }

  static void prepareExplodedWebAppFolder(Project project) {
    // ATTENTION: overlay copy order is important!
    for(String overlay in project.gretty.overlays) {
      def overlayProject = project.project(overlay)
      project.copy {
        from overlayProject.zipTree(getFinalWarPath(overlayProject))
        into "${project.buildDir}/explodedWebapp"
      }
    }
    project.copy {
      from project.zipTree(project.tasks.war.archivePath)
      into "${project.buildDir}/explodedWebapp"
    }
  }
}


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
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class ProjectReloadUtils {

  private static final Logger log = LoggerFactory.getLogger(ProjectReloadUtils)

  private static void addReloadDirs(List<FileReloadSpec> result, Project proj, List reloadSpecs) {
    log.debug 'addReloadDirs reloadSpecs: {}', reloadSpecs
    for(def spec in reloadSpecs) {
      if(spec instanceof Boolean)
        continue
      File baseDir
      def pattern
      def excludesPattern
      if(spec instanceof String)
        baseDir = new File(spec)
      else if(spec instanceof File)
        baseDir = spec
      else if(spec instanceof Map) {
        spec.each { key, value ->
          if(key == 'baseDir')
            baseDir = value instanceof File ? value : new File(value.toString())
          else if(key == 'pattern')
            pattern = value
          else if(key == 'excludesPattern')
            excludesPattern = value
          else
            log.warn 'Unknown reload property: {}', key
        }
        if(!baseDir) {
          log.warn 'Reload property baseDir is not specified.'
          continue
        }
      } else {
        log.warn 'Reload argument is {}. It must be String, File or Map.', spec?.getClass()?.getName()
        continue
      }
      log.debug 'addReloadDirs baseDir: {}', baseDir
      def fileResolver = new FileResolver([ '.', ProjectUtils.getWebAppDir(proj), { it.sourceSets.main.output.files } ])
      fileResolver.acceptDirs = true
      for(File f in fileResolver.resolveFile(proj, baseDir))
        result.add(new FileReloadSpec(baseDir: f, pattern: pattern, excludesPattern: excludesPattern))
    }
  }

  static List<FileReloadSpec> getFastReloadSpecs(Project project, List reloadSpecs) {
    reloadSpecs = reloadSpecs == null ? [] : ([] + reloadSpecs)
    def collectOverlayReloadSpecs
    collectOverlayReloadSpecs = { Project proj ->
      for(def overlay in proj.gretty.overlays.reverse()) {
        overlay = proj.project(overlay)
        if(overlay.extensions.findByName('gretty')) {
          if(overlay.gretty.fastReload != null)
            reloadSpecs.addAll(overlay.gretty.fastReload)
          collectOverlayReloadSpecs(overlay)
        }
      }
    }
    collectOverlayReloadSpecs(project)
    List<FileReloadSpec> result = []
    if(reloadSpecs.find { (it instanceof Boolean) && it }) {
      def addDefaultReloadSpecs
      addDefaultReloadSpecs = { Project proj ->
        def projectReloadSpecs = [ new FileReloadSpec(baseDir: ProjectUtils.getWebAppDir(proj)) ]
        result.addAll(projectReloadSpecs)
        for(def overlay in proj.gretty.overlays)
          addDefaultReloadSpecs(proj.project(overlay))
      }
      addDefaultReloadSpecs(project)
    }
    addReloadDirs(result, project, reloadSpecs)
    log.debug '{} reloadSpecs: {}', project, result
    return result
  }

  static boolean satisfiesOneOfReloadSpecs(String filePath, List<FileReloadSpec> reloadSpecs) {
    reloadSpecs?.find { FileReloadSpec s ->
      if(!filePath.startsWith(s.baseDir.absolutePath))
        return false
      String relPath = filePath - s.baseDir.absolutePath - File.separator
      return (s.pattern == null || relPath =~ s.pattern) && (s.excludesPattern == null || !(relPath =~ s.excludesPattern))
    }
  }
}


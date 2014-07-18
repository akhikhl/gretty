/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class ProjectReloadUtils {

  private static final Logger log = LoggerFactory.getLogger(ProjectReloadUtils)

  private static void addReloadDirs(List<FileReloadSpec> result, Project proj, List reloadSpecs) {
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
      new FileResolver([ '.', ProjectUtils.getWebAppDir(proj), { it.sourceSets.main.output.files } ]).resolveFile(proj, baseDir) {
        result.add(new FileReloadSpec(baseDir: it, pattern: pattern, excludesPattern: excludesPattern))
      }
    }
  }

  static List<FileReloadSpec> getReloadSpecs(Project project, String reloadProperty, List reloadSpecs, Closure defaultReloadSpecsForProject) {
    reloadSpecs = reloadSpecs == null ? [] : ([] + reloadSpecs)
    def collectReloadSpecs
    collectReloadSpecs = { Project proj ->
      if(proj.gretty[reloadProperty] != null)
        reloadSpecs.addAll(proj.gretty[reloadProperty])
      for(def overlay in proj.gretty.overlays.reverse()) {
        overlay = proj.project(overlay)
        if(overlay.extensions.findByName('gretty'))
          collectReloadSpecs(overlay)
      }
    }
    collectReloadSpecs(project)
    List<FileReloadSpec> result = []
    if(reloadSpecs.find { (it instanceof Boolean) && it }) {
      def addDefaultReloadSpecs
      addDefaultReloadSpecs = { Project proj ->
        def projectReloadSpecs = defaultReloadSpecsForProject(proj) ?: []
        result.addAll(projectReloadSpecs)
        for(def overlay in proj.gretty.overlays)
          addDefaultReloadSpecs(proj.project(overlay))
      }
      addDefaultReloadSpecs(project)
    }
    addReloadDirs(result, project, reloadSpecs)
    log.warn '{} : {} reloadSpecs: {}', project, reloadProperty, result
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


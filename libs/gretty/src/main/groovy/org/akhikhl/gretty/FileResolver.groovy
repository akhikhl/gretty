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
final class FileResolver {

  Iterable projectSearchDirs
  Iterable globalSearchDirs
  boolean acceptFiles = true
  boolean acceptDirs = false

  FileResolver(Iterable projectSearchDirs, Iterable globalSearchDirs = null) {
    this.projectSearchDirs = projectSearchDirs
    this.globalSearchDirs = globalSearchDirs
  }

  private Iterable<File> realizeFiles(Project project, Iterable filesOrDirs) {
    def result = []
    def realizeFile
    realizeFile = { file ->
      while(file instanceof Closure)
        file = file(project)
      if(file) {
        if(file instanceof Iterable || file.getClass().isArray())
          for(def f in file)
            realizeFile(f)
        else {
          if(!(file instanceof File))
            file = new File(file.toString())
          if(file.isAbsolute())
            result.add(file)
          else if(project != null)
            result.add(new File(project.projectDir, file.path))
        }
      }
    }
    realizeFile(filesOrDirs)
    result
  }

  Set<File> resolveFile(Project project, file) {
    Set<File> result = new LinkedHashSet()
    if(file != null) {
      if(file instanceof Iterable || file.getClass().isArray())
        // findResults excludes null values
        for(def f in file.findResults({ it }))
          resolveFile_(result, project, f)
      else
        resolveFile_(result, project, file)
    }
    return result
  }

  private resolveFile_(Collection<File> result, Project project, file) {
    if(!(file instanceof File))
      file = new File(file.toString())
    if(file.isAbsolute())
      result.add(file)
    else if(project != null)
      resolveFileOnProjectAndOverlays(result, project, file)
    for(File dir in realizeFiles(null, globalSearchDirs)) {
      File f = new File(dir, file.path)
      if(f.exists())
        result.add(f)
    }
  }

  protected resolveFileOnProject(Collection<File> result, Project project, File file) {
    for(File dir in realizeFiles(project, projectSearchDirs)) {
      File f = new File(dir, file.path)
      if(f.exists() && ((acceptFiles && f.isFile()) || (acceptDirs && f.isDirectory())))
        result.add(f)
    }
  }

  protected resolveFileOnProjectAndOverlays(Collection<File> result, Project project, File file) {
    resolveFileOnProject(result, project, file)
    if(project.extensions.findByName('gretty'))
      for(def overlay in project.gretty.overlays.reverse())
        resolveFileOnProjectAndOverlays(result, project.project(overlay), file)
  }

  File resolveSingleFile(Project project, file) {
    Set<File> files = resolveFile(project, file)
    files ? files[0] : null
  }
}

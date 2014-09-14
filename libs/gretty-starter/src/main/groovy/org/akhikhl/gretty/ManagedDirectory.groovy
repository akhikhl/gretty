/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class ManagedDirectory {

  protected static final Logger log = LoggerFactory.getLogger(ManagedDirectory)

  final File baseDir
  private final Set addedDirs = new HashSet()
  private final Set addedFiles = new HashSet()

  ManagedDirectory(File baseDir) {
    this.baseDir = baseDir
    addedDirs.add(baseDir)
  }
  
  void add(File srcFile) {
    add(srcFile, null)
  }
  
  void add(File srcFile, String dstSubDir) {
    File dstFile = dstSubDir ? new File(new File(baseDir, dstSubDir), srcFile.name) : new File(baseDir, srcFile.name)
    add_(srcFile, dstFile)
  }
  
  private void add_(File srcFile, File dstFile) {
    if(srcFile.isDirectory()) {
      dstFile.mkdirs()
      registerAdded(dstFile)
      for(File f in srcFile.listFiles())
        add_(f, new File(dstFile, f.name))
    } else {
      dstFile.parentFile.mkdirs()
      FileUtils.copyFile(srcFile, dstFile)
      registerAdded(dstFile)
    }
  }
  
  void cleanup() {
    cleanupFiles(baseDir)
  }
  
  private void cleanupFiles(File dir) {
    if(addedDirs.contains(dir)) {
      for(File f in dir.listFiles()) {
        if(f.isFile()) {
          if(!addedFiles.contains(f)) {
            log.debug 'deleting managed {}', f
            f.delete()
          }
        } else
          cleanupFiles(f)
      }
    } else {
      log.debug 'deleting managed {}', dir
      dir.deleteDir()
    }
  }
  
  void registerAdded(File f) {
    if(f.isDirectory()) {
      while(f != baseDir) {
        if(addedDirs.add(f))
          log.debug 'added managed {}', f
        f = f.parentFile
      }
    } else {
      registerAdded(f.parentFile)
      if(addedFiles.add(f))
        log.debug 'added managed {}', f      
    }
  }
}

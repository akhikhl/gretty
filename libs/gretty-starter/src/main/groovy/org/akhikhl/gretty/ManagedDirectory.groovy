/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
class ManagedDirectory {

  File baseDir
  Set addedDirs = new HashSet()
  Set addedFiles = new HashSet()

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
      registerAddedDir(dstFile)
      for(File f in srcFile.listFiles())
        add_(f, new File(dstFile, f.name))
    } else {
      dstFile.parentFile.mkdirs()
      registerAddedDir(dstFile.parentFile)
      FileUtils.copyFile(srcFile, dstFile)
      addedFiles.add(dstFile)
    }
  }
  
  void cleanup() {
    cleanupFiles(baseDir)
  }
  
  private void cleanupFiles(File dir) {
    if(addedDirs.contains(dir)) {
      for(File f in dir.listFiles()) {
        if(f.isFile()) {
          if(!addedFiles.contains(f))
            f.delete()
        } else
          cleanupFiles(f)
      }
    } else
      dir.deleteDir()
  }
  
  private void registerAddedDir(File dir) {
    while(dir != baseDir) {
      addedDirs.add(dir)
      dir = dir.parentFile
    }
  }
}

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
class StarterLauncher extends LauncherBase {

  StarterLauncher(LauncherConfig config) {
    super(config)
  }

  @Override
  protected Collection<URL> getRunnerClassPath() {
    []
  }

  @Override
  protected String getServletContainerName() {
    'test'
  }

  @Override
  protected void javaExec(JavaExecParams params) {
    println "javaExec $params"
  }
}


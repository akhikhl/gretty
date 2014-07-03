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

  private final File basedir
  private final Map servetContainerInfo

  StarterLauncher(File basedir, Map servetContainerInfo, LauncherConfig config) {
    super(config)
    this.basedir = basedir
    this.servetContainerInfo = servetContainerInfo
  }

  @Override
  protected String getServletContainerDescription() {
    servetContainerInfo.description
  }

  @Override
  protected void javaExec(JavaExecParams params) {
    String javaPath = new File(System.getProperty("java.home"), 'bin/java').absolutePath
    String classPath1 = [ basedir.absolutePath, 'conf' ].join(File.separator)
    String classPath2 = [ basedir.absolutePath, 'runner', '*' ].join(File.separator)
    String classPath = classPath1 + System.getProperty('path.separator') + classPath2
    def procParams = [ javaPath ] + params.jvmArgs + [ '-cp', classPath, params.main ] + params.args
    Process proc = procParams.execute()
    proc.waitForProcessOutput(System.out, System.err)
  }
}


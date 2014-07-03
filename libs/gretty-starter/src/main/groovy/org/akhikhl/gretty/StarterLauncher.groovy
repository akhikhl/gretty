/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class StarterLauncher extends LauncherBase {

  protected static final Logger log = LoggerFactory.getLogger(StarterLauncher)

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
    def procParams = [ javaPath ] + params.jvmArgs + params.systemProperties.collect { k, v -> "-D$k=$v" } + [ '-cp', classPath, params.main ] + params.args
    log.debug 'Launching runner process: {}', procParams.join(' ')
    Process proc = procParams.execute()
    proc.waitForProcessOutput(System.out, System.err)
  }
}

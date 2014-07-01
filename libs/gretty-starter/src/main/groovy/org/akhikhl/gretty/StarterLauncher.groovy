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
    config.getServerConfig().servletContainer
  }

  @Override
  protected void javaExec(JavaExecParams params) {
    println "javaExec $params"
    def javaPath = new File(System.getProperty("java.home"), 'bin/java').absolutePath
    def procParams = [ javaPath ] + params.jvmArgs + [ params.main ] + params.args
    ProcessBuilder pb = new ProcessBuilder(procParams as String[])
    Process proc = pb.start()
    proc.consumeProcessOutput(System.out, System.err)
    proc.waitFor()
  }
}


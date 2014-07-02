/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.process.JavaExecSpec
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class DefaultLauncher extends LauncherBase {

  protected Project project

  DefaultLauncher(Project project, LauncherConfig config) {
    super(config)
    this.project = project
  }

  @Override
  protected Collection<URL> getRunnerClassPath() {
    (project.configurations.gretty + project.configurations[getServletContainerConfig().servletContainerRunnerConfig]).files.collect { it.toURL() }
  }

  protected Map getServletContainerConfig() {
    ServletContainerConfig.getConfig(sconfig.servletContainer)
  }

  @Override
  protected String getServletContainerName() {
    getServletContainerConfig().fullName
  }

  @Override
  protected void javaExec(JavaExecParams params) {
    project.javaexec { JavaExecSpec spec ->
      spec.classpath = project.files(params.classpath)
      spec.debug = params.debug
      spec.jvmArgs = params.jvmArgs
      spec.systemProperties = params.systemProperties
      spec.main = params.main
      spec.args = params.args
    }
  }
}

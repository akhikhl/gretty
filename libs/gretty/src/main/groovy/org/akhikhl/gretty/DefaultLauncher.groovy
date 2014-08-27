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

  protected Collection<URL> getRunnerClassPath() {
    (project.configurations.gretty + project.configurations[getServletContainerConfig().servletContainerRunnerConfig]).files.collect { it.toURL() }
  }

  protected Map getServletContainerConfig() {
    ServletContainerConfig.getConfig(sconfig.servletContainer)
  }

  @Override
  protected String getServletContainerId() {
    sconfig.servletContainer
  }

  @Override
  protected String getServletContainerDescription() {
    getServletContainerConfig().servletContainerDescription
  }

  @Override
  protected void javaExec(JavaExecParams params) {
    project.javaexec { JavaExecSpec spec ->
      def runnerClasspath = getRunnerClassPath()
      log.debug 'Runner classpath: {}', runnerClasspath
      spec.classpath = project.files(runnerClasspath)
      spec.debug = params.debug
      spec.jvmArgs params.jvmArgs
      spec.systemProperties params.systemProperties
      spec.main = params.main
      spec.args = params.args
    }
  }

  @Override
  protected void prepareToRun(WebAppConfig wconfig) {
    ProjectUtils.prepareToRun(project, wconfig)
  }
}

/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty
import org.gradle.api.Project
import org.gradle.process.JavaExecSpec
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

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
      if(log.isDebugEnabled())
        for(def path in runnerClasspath)
          log.debug 'Runner classpath: {}', path
      spec.classpath = project.files(runnerClasspath)
      def jvmArgs = params.jvmArgs
      if(params.debug) {
        jvmArgs.add '-Xdebug'
        String debugArg = "-Xrunjdwp:transport=dt_socket,server=y,suspend=${params.debugSuspend ? 'y' : 'n'},address=${params.debugPort}"
        jvmArgs.add debugArg
        log.warn 'DEBUG MODE, port={}, suspend={}', params.debugPort, params.debugSuspend
      }
      spec.jvmArgs jvmArgs
      spec.systemProperties params.systemProperties
      spec.main = params.main
      spec.args = params.args
    }
  }

  @Override
  protected void prepareToRun(WebAppConfig wconfig) {
    ProjectUtils.prepareToRun(project, wconfig)
  }

  @Override
  protected void rebuildWebapps() {
    webAppConfigs.each { WebAppConfig wconfig ->
      if(wconfig.projectPath) {
        def proj = project.project(wconfig.projectPath)
        ProjectConnection connection = GradleConnector.newConnector().useInstallation(proj.gradle.gradleHomeDir).forProjectDirectory(proj.projectDir).connect()
        try {
          connection.newBuild().forTasks(wconfig.inplace ? 'prepareInplaceWebApp' : 'prepareArchiveWebApp').run()
        } finally {
          connection.close()
        }
      }
    }
  }
}

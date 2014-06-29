/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonBuilder
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

  protected FileCollection getRunnerClassPath() {
    project.configurations.gretty + project.configurations[getServletContainerConfig().servletContainerRunnerConfig]
  }

  protected Map getServletContainerConfig() {
    ServletContainerConfig.getConfig(sconfig.servletContainer)
  }

  protected String getServletContainerName() {
    getServletContainerConfig().fullName
  }

  protected void launchProcess() {
    
    project.javaexec { JavaExecSpec spec ->

      def cmdLineJson = getCommandLineJson()
      log.debug 'Command-line json: {}', cmdLineJson.toPrettyString()
      cmdLineJson = cmdLineJson.toString()

      // we are going to pass json as argument to java process.
      // under windows we must escape double quotes in process parameters.
      if(System.getProperty("os.name") =~ /(?i).*windows.*/)
        cmdLineJson = cmdLineJson.replace('"', '\\"')

      if(log.isDebugEnabled())
        getRunnerClassPath().each {
          log.debug 'runnerclasspath: {}', it
        }
      spec.classpath = getRunnerClassPath()

      spec.main = 'org.akhikhl.gretty.Runner'
      spec.args = [ cmdLineJson ]

      spec.debug = config.getDebug()

      log.debug 'server-config jvmArgs: {}', sconfig.jvmArgs
      spec.jvmArgs sconfig.jvmArgs

      if(config.getJacocoConfig()) {
        String jarg = config.getJacocoConfig().getAsJvmArg()
        log.debug 'jacoco jvmArgs: {}', jarg
        spec.jvmArgs jarg
      }

      if(config.getManagedClassReload()) {
        spec.jvmArgs '-javaagent:' + project.configurations.grettySpringLoaded.singleFile.absolutePath, '-noverify'
        spec.systemProperty 'springloaded', 'exclusions=org.akhikhl.gretty..*'
      }

      // Speeding up tomcat startup, according to http://wiki.apache.org/tomcat/HowTo/FasterStartUp
      // ATTENTION: replacing the blocking entropy source (/dev/random) with a non-blocking one
      // actually reduces security because you are getting less-random data.
      spec.systemProperty 'java.security.egd', 'file:/dev/./urandom'      
    }
  }
}

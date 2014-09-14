/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import java.util.concurrent.ExecutorService
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

/**
 * Base task for starting jetty
 *
 * @author akhikhl
 */
abstract class StartBaseTask extends DefaultTask {

  boolean interactive = true
  boolean debug = false

  private JacocoHelper jacocoHelper

  protected final List<Closure> prepareServerConfigClosures = []
  protected final List<Closure> prepareWebAppConfigClosures = []

  def serverStartInfo

  @TaskAction
  void action() {
    LauncherConfig config = getLauncherConfig()
    Launcher launcher = ProjectUtils.anyWebAppUsesSpringBoot(project, config.getWebAppConfigs()) ? new SpringBootLauncher(project, config) : new DefaultLauncher(project, config)
    launcher.scannerManager = new JettyScannerManager(project, config.getServerConfig(), config.getWebAppConfigs(), config.getManagedClassReload())
    if(getIntegrationTest())
      project.ext.grettyLaunchThread = launcher.launchThread()
    else
      launcher.launch()
    serverStartInfo = launcher.serverStartInfo
  }

  protected final void doPrepareServerConfig(ServerConfig sconfig) {

    CertificateGenerator.maybeGenerate(project, sconfig)

    String jacocoConfigJvmArg = getJacoco()?.getAsJvmArg()
    if(jacocoConfigJvmArg)
      sconfig.jvmArgs jacocoConfigJvmArg

    if(getManagedClassReload(sconfig)) {
      sconfig.jvmArgs '-javaagent:' + project.configurations.grettySpringLoaded.singleFile.absolutePath, '-noverify'
      sconfig.systemProperty 'springloaded', 'exclusions=org.akhikhl.gretty..*'
    }

    for(Closure c in prepareServerConfigClosures) {
      c = c.rehydrate(sconfig, c.owner, c.thisObject)
      c.resolveStrategy = Closure.DELEGATE_FIRST
      c()
    }
  }

  protected final void doPrepareWebAppConfig(WebAppConfig wconfig) {
    for(Closure c in prepareWebAppConfigClosures) {
      c = c.rehydrate(wconfig, c.owner, c.thisObject)
      c.resolveStrategy = Closure.DELEGATE_FIRST
      c()
    }
  }

  protected boolean getDefaultJacocoEnabled() {
    false
  }

  protected boolean getIntegrationTest() {
    false
  }

  JacocoTaskExtension getJacoco() {
    if(jacocoHelper == null && project.extensions.findByName('jacoco')) {
      jacocoHelper = new JacocoHelper(this)
      jacocoHelper.jacoco.enabled = getDefaultJacocoEnabled()
    }
    jacocoHelper?.jacoco
  }

  protected final LauncherConfig getLauncherConfig() {

    def self = this
    def startConfig = getStartConfig()

    new LauncherConfig() {

      boolean getDebug() {
        self.debug
      }

      boolean getInteractive() {
        self.getInteractive()
      }

      boolean getManagedClassReload() {
        self.getManagedClassReload(startConfig.getServerConfig())
      }

      ServerConfig getServerConfig() {
        startConfig.getServerConfig()
      }

      String getStopCommand() {
        self.getStopCommand()
      }

      File getBaseDir() {
        new File(self.project.buildDir, 'serverBaseDir_' + getServerConfig().servletContainer)
      }

      WebAppClassPathResolver getWebAppClassPathResolver() {
        new WebAppClassPathResolver() {
          Collection<URL> resolveWebAppClassPath(WebAppConfig wconfig) {
            def resolvedClassPath = new LinkedHashSet<URL>()
            if(wconfig.projectPath) {
              def proj = self.project.project(wconfig.projectPath)
              String runtimeConfig = ProjectUtils.isSpringBootApp(proj) ? 'springBoot' : 'runtime'
              resolvedClassPath.addAll(ProjectUtils.getClassPath(proj, wconfig.inplace, runtimeConfig))
              resolvedClassPath.addAll(ProjectUtils.resolveClassPath(proj, wconfig.classPath))
              // exclude groovy-all from webapp classpath, so that there's no conflict with groovy-all inherited from web-server.
              resolvedClassPath = resolvedClassPath.findAll { !it.toString().contains('groovy-all') }
            }
            resolvedClassPath
          }
        }
      }

      Iterable<WebAppConfig> getWebAppConfigs() {
        startConfig.getWebAppConfigs()
      }
    }
  }

  protected boolean getManagedClassReload(ServerConfig sconfig) {
    sconfig.managedClassReload
  }

  protected abstract StartConfig getStartConfig()

  protected abstract String getStopCommand()

  final void jacoco(Closure configureClosure) {
    getJacoco()?.with configureClosure
  }

  final void prepareServerConfig(Closure closure) {
    prepareServerConfigClosures.add(closure)
  }

  final void prepareWebAppConfig(Closure closure) {
    prepareWebAppConfigClosures.add(closure)
  }
}

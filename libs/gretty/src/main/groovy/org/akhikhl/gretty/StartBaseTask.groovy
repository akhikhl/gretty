/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.akhikhl.gretty.scanner.JDKScannerManager
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.internal.TaskInternal
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaForkOptions
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.springframework.boot.devtools.autoconfigure.OptionalLiveReloadServer
import org.springframework.boot.devtools.livereload.LiveReloadServer
/**
 * Base task for starting jetty
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
abstract class StartBaseTask extends DefaultTask {

  boolean interactive = true
  boolean debug = false
  Integer debugPort
  Boolean debugSuspend

  private JacocoHelper jacocoHelper

  protected final List<Closure> prepareServerConfigClosures = []
  protected final List<Closure> prepareWebAppConfigClosures = []

  Map serverStartInfo

  StartBaseTask() {
    initJacoco()
    getOutputs().upToDateWhen { false }
  }

  @TaskAction
  void action() {
    LauncherConfig config = getLauncherConfig()
    Launcher launcher = new DefaultLauncher(project, config)
    if(config.serverConfig.liveReloadEnabled) {
      launcher.optionalLiveReloadServer = new OptionalLiveReloadServer(new LiveReloadServer())
    }
    launcher.scannerManager = createScannerManager(config, launcher.optionalLiveReloadServer)
    if(getIntegrationTest()) {
      boolean result = false
      try {
        launcher.beforeLaunch()
        try {
          project.ext.grettyLauncher = launcher
          project.ext.grettyLaunchThread = launcher.launchThread()
          result = true
        } finally {
          // Need to call afterLaunch in case of unsuccessful launch.
          // If launch was successful, afterLaunch will be called in AppAfterIntegrationTestTask or FarmAfterIntegrationTestTask
          if(!result)
            launcher.afterLaunch()
        }
      } finally {
        // Need to dispose of launcher in case of unsuccessful launch.
        // If launch was successful, launcher will be shut down in AppAfterIntegrationTestTask or FarmAfterIntegrationTestTask
        if(!result)
          launcher.dispose()
      }
    }
    else {
      try {
        launcher.launch()
      } finally {
        launcher.dispose()
      }
    }
    serverStartInfo = launcher.getServerStartInfo()
  }

  private ScannerManager createScannerManager(LauncherConfig config, OptionalLiveReloadServer optionalLiveReloadServer) {
    switch (config.serverConfig.scanner) {
      case 'jdk':
        if(JDKScannerManager.available()) {
          return new JDKScannerManager(project, config.serverConfig, config.webAppConfigs, config.managedClassReload)
        } else {
          logger.error('JDK scanner was specified but it\'s not available. Falling back to jetty scanner')
          return new JettyScannerManager(project, config.serverConfig, config.webAppConfigs, config.managedClassReload)
        }
      case 'jetty':
        return new JettyScannerManager(project, config.serverConfig, config.webAppConfigs, config.managedClassReload)
      default:
        throw new IllegalArgumentException("Unknown scanner config: ${config.serverConfig.scanner}")
    }
  }

  protected final void doPrepareServerConfig(ServerConfig sconfig) {

    CertificateGenerator.maybeGenerate(project, sconfig)

    if(getJacoco()?.enabled) {
      String jacocoConfigJvmArg = getJacoco().getAsJvmArg()
      if(jacocoConfigJvmArg)
        sconfig.jvmArgs jacocoConfigJvmArg
    }

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
    jacocoHelper?.jacoco
  }

  private void initJacoco() {
    if(project.extensions.findByName('jacoco') && project.gretty.jacocoEnabled) {
      Task startTask = this
      jacocoHelper = (TaskInternal.methods.collectEntries({ [it.name, {} ] }) +
          JavaForkOptions.methods.collectEntries({ [it.name, {} ] }) +
          ExtensionAware.methods.collectEntries({ [it.name, {} ] }) + [
          getExtensions: { startTask.getExtensions() },
          getInputs: { startTask.getInputs() },
          getJacoco: { startTask.extensions.jacoco },
          getName: { startTask.getName() },
          getOutputs: { startTask.getOutputs() },
          getProject: { startTask.project },
          getWorkingDir: { project.projectDir },
          getJvmArgumentProviders: { [] }
      ]) as JacocoHelper
      project.jacoco.applyTo(jacocoHelper)
      jacocoHelper.jacoco.enabled = getDefaultJacocoEnabled()
    }
  }

  protected final LauncherConfig getLauncherConfig() {

    def self = this
    def startConfig = getStartConfig()

    new LauncherConfig() {

      boolean getDebug() {
        self.debug
      }

      int getDebugPort() {
        self.debugPort == null ? self.project.gretty.debugPort : self.debugPort
      }

      boolean getDebugSuspend() {
        self.debugSuspend == null ? self.project.gretty.debugSuspend : self.debugSuspend
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

      boolean getProductMode() {
        false
      }

      WebAppClassPathResolver getWebAppClassPathResolver() {
        new WebAppClassPathResolver() {
          Collection<URL> resolveWebAppClassPath(WebAppConfig wconfig) {
            Set<URL> resolvedClassPath = new LinkedHashSet<URL>()
            if(wconfig.projectPath) {
              def proj = self.project.project(wconfig.projectPath)
              String runtimeConfig = ProjectUtils.isSpringBootApp(proj) ? 'springBoot' : 'runtimeClasspath'
              resolvedClassPath.addAll(ProjectUtils.resolveClassPath(proj, wconfig.beforeClassPath))
              resolvedClassPath.addAll(ProjectUtils.getClassPath(proj, wconfig.inplace, runtimeConfig))
              resolvedClassPath.addAll(ProjectUtils.resolveClassPath(proj, wconfig.classPath))
            } else {
              for (def classpath in [wconfig.beforeClassPath, wconfig.classPath]) {
                if (classpath) {
                  for (String elem in classpath) {
                    URL url
                    if (elem =~ /.{2,}\:.+/)
                      url = new URL(elem)
                    else
                      url = new File(elem).toURI().toURL()
                    resolvedClassPath.add(url)
                  }
                }
              }
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

  Collection<URL> getRunnerClassPath() {
    DefaultLauncher.getRunnerClassPath(project, getStartConfig().serverConfig)
  }

  protected abstract StartConfig getStartConfig()

  protected abstract String getStopCommand()

  /**
   * key is context path, value is collection of classpath URLs
   * @return
   */
  Map<String, Collection<URL> > getWebappClassPaths() {
    LauncherConfig config = getLauncherConfig()
    WebAppClassPathResolver resolver = config.getWebAppClassPathResolver()
    config.getWebAppConfigs().collectEntries { wconfig ->
      [ wconfig.contextPath, resolver.resolveWebAppClassPath(wconfig) ]
    }
  }

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

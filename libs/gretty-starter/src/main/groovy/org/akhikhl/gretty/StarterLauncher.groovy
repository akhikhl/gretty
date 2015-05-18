/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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
  private final Map starterConfig

  StarterLauncher(File basedir, Map starterConfig, LauncherConfig config) {
    super(config)
    this.basedir = basedir
    this.starterConfig = starterConfig
  }

  @Override
  protected String getServletContainerId() {
    starterConfig.servletContainer.id
  }

  @Override
  protected String getServletContainerDescription() {
    starterConfig.servletContainer.description
  }

  @Override
  protected void javaExec(JavaExecParams params) {
    String javaExe = PlatformUtils.isWindows() ? 'java.exe' : 'java'
    String javaPath = new File(System.getProperty("java.home"), "bin/$javaExe").absolutePath
    def classPath = [ new File(basedir, 'runner/*'), new File(basedir, 'runner/logback-config') ]
    for(WebAppConfig wconfig in config.getWebAppConfigs())
      if(wconfig.springBoot) {
        File classesDir = new File(wconfig.resourceBase, 'WEB-INF/classes')
        if(classesDir.exists())
          classPath.add(classesDir)
        File libDir = new File(wconfig.resourceBase, 'WEB-INF/lib')
        if(libDir.exists())
          classPath.add(new File(libDir, '*'))
      }
    classPath = classPath.collect { it.absolutePath }.join(System.getProperty('path.separator'))
    // Note that JavaExecParams debugging properties are intentionally ignored.
    // It is supposed that webapp debugging is performed via DefaultLauncher.
    def procParams = [ javaPath ] + params.jvmArgs + ['-DgrettyProduct=true'] + params.systemProperties.collect { k, v -> "-D$k=$v" } + [ '-cp', classPath, params.main ] + params.args
    log.debug 'Launching runner process: {}', procParams.join(' ')
    Process proc = procParams.execute()
    proc.waitForProcessOutput(System.out, System.err)
  }

  @Override
  protected void rebuildWebapps() {
    println 'Cannot rebuild application in StarterLauncher'
  }
}

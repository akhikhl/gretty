/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import org.akhikhl.gretty.DefaultLauncher
import org.akhikhl.gretty.WebAppConfig
import org.gradle.api.file.FileCollection

/**
 *
 * @author akhikhl
 */
class SpringBootLauncher extends DefaultLauncher {

  @Override
  protected String getRunnerClassName() {
    'org.akhikhl.gretty.springboot.Runner'
  }
  
  @Override
  protected FileCollection getRunnerClassPath() {
    def files = project.configurations.grettyNoSpringBoot.files
    for(def wconfig in webAppConfigs)
      files += resolveWebAppClassPath(wconfig)
    project.files(files)
  }
  
  @Override
  protected String getRunnerRuntimeConfig() {
    'springBoot'
  }
  
  @Override
  protected writeRunConfigJson(json) {
    super.writeRunConfigJson(json)
    json.with {
      springBootMainClass SpringBootMainClassFinder.findMainClass(project)
    }
  }
  
  protected void writeWebAppClassPath(json, WebAppConfig webAppConfig) {
    // webapp classpath is passed to the runner
  }
}

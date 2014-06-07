/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import org.akhikhl.gretty.DefaultLauncher

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
}

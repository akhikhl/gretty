/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class SpringBootMainClassFinder {

  static String findMainClass(Project project) {

    def bootExtension = project.extensions.findByName('springBoot')
    if(bootExtension && bootExtension.mainClass)
      return bootExtension.mainClass

    def MainClassFinder = Class.forName('org.springframework.boot.loader.tools.MainClassFinder', true, SpringBootMainClassFinder.classLoader)

    if(MainClassFinder.metaClass.methods.find { it.name == 'findSingleMainClass' })
      // spring-boot 1.1.x
      return MainClassFinder.findSingleMainClass(project.sourceSets.main.output.classesDir)

    // spring-boot 1.0.x
    return MainClassFinder.findMainClass(project.sourceSets.main.output.classesDir)
  }
}

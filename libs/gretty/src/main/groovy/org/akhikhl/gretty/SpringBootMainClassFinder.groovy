/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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
    
    if(MainClassFinder.metaClass.methods.find { it.name == 'findSingleMainClass' }) {
      // spring-boot 1.1.x
      def findInProject
      findInProject = { Project proj ->
        if(proj.hasProperty('sourceSets')) {
          String result = MainClassFinder.findSingleMainClass(proj.sourceSets.main.output.classesDir)
          if(result)
            return result
        }
        proj.subprojects.findResult findInProject
      }
      return findInProject(project)
    }

    // spring-boot 1.0.x
    def findInProject
    findInProject = { Project proj ->
      if(proj.hasProperty('sourceSets')) {
        String result = MainClassFinder.findMainClass(proj.sourceSets.main.output.classesDir)
        if(result)
          return result
      }
      proj.subprojects.findResult findInProject
    }
    return findInProject(project)
  }
}


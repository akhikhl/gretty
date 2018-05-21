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

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class SpringBootMainClassFinder {

  protected static Iterable<File> getClassesDirs(Project project) {
    if(project.gradle.gradleVersion.startsWith('1.') || project.gradle.gradleVersion.startsWith('2.'))
      return [ project.sourceSets.main.output.classesDir ]
    project.sourceSets.main.output.classesDirs
  }

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
          for(File classesDir in getClassesDirs(proj)) {
            String result = MainClassFinder.findSingleMainClass(classesDir)
            if (result)
              return result
          }
        }
        proj.subprojects.findResult findInProject
      }
      return findInProject(project)
    }

    // spring-boot 1.0.x
    def findInProject
    findInProject = { Project proj ->
      if(proj.hasProperty('sourceSets')) {
        for(File classesDir in getClassesDirs(proj)) {
          String result = MainClassFinder.findMainClass(classesDir)
          if (result)
            return result
        }
      }
      proj.subprojects.findResult findInProject
    }
    return findInProject(project)
  }
}


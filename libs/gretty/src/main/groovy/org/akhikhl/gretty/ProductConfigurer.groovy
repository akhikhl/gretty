/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class ProductConfigurer {

  protected final Project project
  protected final File outputDir
  protected final AntBuilder ant

  ProductConfigurer(Project project) {
    this.project = project
    outputDir = new File(project.buildDir, 'output')
    ant = new AntBuilder()
  }

  void configureProduct(String servletContainer) {

    def buildProductTask = project.task('buildProduct_' + servletContainer, group: 'gretty') {

      dependsOn project.tasks.build

      doLast {
        println "building product for $servletContainer"
      }
    }

    project.task('archiveProduct_' + servletContainer, group: 'gretty') {

      dependsOn buildProductTask

      doLast {
        println "archiving product for $servletContainer"
      }
    }
  }

  void configureProducts() {

    project.afterEvaluate {

      for(String servletContainer in ServletContainerConfig.getConfigNames())
        configureProduct(servletContainer)

      ServerConfig defaultServerConfig = new ServerConfig()
      ConfigUtils.complementProperties(defaultServerConfig, project.gretty.serverConfig, ProjectUtils.getDefaultServerConfig(project))

      project.task('buildProduct') {
        dependsOn project.tasks['buildProduct_' + defaultServerConfig.servletContainer]
      }

      project.task('archiveProduct') {
        dependsOn project.tasks.buildProduct
        dependsOn project.tasks['archiveProduct_' + defaultServerConfig.servletContainer]
      }
    }
  }
}

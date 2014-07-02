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
import org.akhikhl.gradle.onejar.OneJarConfigurer
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class ProductConfigurer {

  protected Project project
  protected AntBuilder ant = new AntBuilder()

  ProductConfigurer(Project project) {
    this.project = project
  }

  void configureProducts() {

    new OneJarConfigurer([ runTaskName: 'runProduct', debugTaskName: 'debugProduct', suppressBuildTaskDependency: true, runtimeConfiguration: 'grettyStarter' ], project).apply()

    ServerConfig defaultServerConfig = new ServerConfig()
    ConfigUtils.complementProperties(defaultServerConfig, project.gretty.serverConfig, ProjectUtils.getDefaultServerConfig(project))

    project.onejar {

      mainJar = { project.configurations.grettyStarterMain.singleFile }
      mainClass = 'org.akhikhl.gretty.GrettyStarter'

      ServletContainerConfig.getConfigNames().each { servletContainer ->
        def servletContainerConfig = ServletContainerConfig.getConfig(servletContainer)
        product configBaseName: 'gretty', suffix: servletContainer, launchers: [ 'shell', 'windows' ],
          productInfo: [ 'servletContainer': servletContainerConfig.fullName ],
          additionalProductFiles: { Map product ->
            //(project.configurations.gretty + project.configurations[servletContainerConfig.servletContainerRunnerConfig]).files
            [ createStarterConfig(product) ]
          }
      }

      afterEvaluate {
        project.task('buildProduct', dependsOn: 'buildProduct_' + defaultServerConfig.servletContainer)
      }
    }
  }

  File createStarterConfig(Map product) {

    File starterConfigDir = new File(project.buildDir, 'tmp/gretty-starter-config')

    Gson gson = new GsonBuilder().setPrettyPrinting().create()

    ServerConfig sconfig = new ServerConfig()
    ConfigUtils.complementProperties(sconfig, project.gretty.serverConfig, ProjectUtils.getDefaultServerConfig(project))
    sconfig.servletContainer = product.suffix
    ProjectUtils.resolveServerConfig(project, sconfig)

    CertificateGenerator.maybeGenerate(project, sconfig).each {
      ant.copy file: it, tofile: new File(starterConfigDir, "gretty-starter/ssl/${it.name}")
    }

    WebAppConfig wconfig = new WebAppConfig()
    ConfigUtils.complementProperties(wconfig, project.gretty.webAppConfig, ProjectUtils.getDefaultWebAppConfigForProject(project))
    ProjectUtils.resolveWebAppConfig(project, wconfig, sconfig.servletContainer)

    File starterConfig = new File(starterConfigDir, 'gretty-starter/gretty-starter-config.json')
    starterConfig.withWriter {
      gson.toJson([ sconfig: sconfig, webapps: [ wconfig ] ], it)
    }

    File starterConfigJar = new File(project.buildDir, 'tmp/gretty-starter-config.jar')
    ant.jar destfile: starterConfigJar, {
      fileset(dir: starterConfigDir, includes: '**')
    }
    starterConfigJar
  }
}


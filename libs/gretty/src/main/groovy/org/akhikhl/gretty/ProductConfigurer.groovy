/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.akhikhl.gradle.onejar.OneJarConfigurer
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class ProductConfigurer {

  protected Project project

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
        product configBaseName: 'gretty', suffix: servletContainer, launchers: [ 'shell', 'windows' ],
          productInfo: [ 'servletContainer': ServletContainerConfig.getConfig(servletContainer).fullName ],
          additionalProductFiles: { Map product ->
            [ createLauncherConfigFile(product) ]
          }
      }

      afterEvaluate {
        project.task('buildProduct', dependsOn: 'buildProduct_' + defaultServerConfig.servletContainer)
      }

      onProductGeneration { product, outputDir ->

        ServerConfig sconfig = new ServerConfig()
        ConfigUtils.complementProperties(sconfig, project.gretty.serverConfig, ProjectUtils.getDefaultServerConfig(project))
        sconfig.servletContainer = product.suffix
        ProjectUtils.resolveServerConfig(project, sconfig)
        CertificateGenerator.maybeGenerate(project, sconfig)

        WebAppConfig wconfig = new WebAppConfig()
        ConfigUtils.complementProperties(wconfig, project.gretty.webAppConfig, ProjectUtils.getDefaultWebAppConfigForProject(project))
        ProjectUtils.resolveWebAppConfig(project, wconfig, sconfig.servletContainer)
      }
    }
  }

  File createLauncherConfigFile(Map product) {
    File launcherConfigFile = new File(project.buildDir, "tmp/launcherConfigFile-${product.suffix}.json")
    launcherConfigFile.text = '{ "title": "just a test that it works" }'
    launcherConfigFile
  }
}


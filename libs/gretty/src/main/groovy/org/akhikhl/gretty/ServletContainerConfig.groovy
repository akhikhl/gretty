/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.GradleException

/**
 *
 * @author akhikhl
 */
class ServletContainerConfig {

	private static configs = createConfigs()

  private static createConfigs() {
    String grettyVersion = Externalized.getString('grettyVersion')
    String jetty7Version = Externalized.getString('jetty7Version')
    String jetty7ServletApi = Externalized.getString('jetty7ServletApi')
    String jetty8Version = Externalized.getString('jetty8Version')
    String jetty8ServletApi = Externalized.getString('jetty8ServletApi')
    String jetty9Version = Externalized.getString('jetty9Version')
    String jetty9ServletApi = Externalized.getString('jetty9ServletApi')
    String tomcat7Version = Externalized.getString('tomcat7Version')
    String tomcat7ServletApi = Externalized.getString('tomcat7ServletApi')
    String tomcat8Version = Externalized.getString('tomcat8Version')
    String tomcat8ServletApi = Externalized.getString('tomcat8ServletApi')
    [ 'jetty7': [
        servletContainerType: 'jetty',
        servletContainerVersion: jetty7Version,
        fullName: "Jetty $jetty7Version",
        servletContainerRunnerConfig: 'grettyRunnerJetty7',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "org.akhikhl.gretty:gretty-runner-jetty7:$grettyVersion"
        },
        servletApiDependencies: { project ->
          project.dependencies {
            providedCompile jetty7ServletApi
          }          
        }
      ],
      'jetty8': [
        servletContainerType: 'jetty',
        servletContainerVersion: jetty8Version,
        fullName: "Jetty $jetty8Version",
        servletContainerRunnerConfig: 'grettyRunnerJetty8',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "org.akhikhl.gretty:gretty-runner-jetty8:$grettyVersion"
        },
        servletApiDependencies: { project ->
          project.dependencies {
            providedCompile jetty8ServletApi
          }          
        }
      ],
      'jetty9': [
        servletContainerType: 'jetty',
        servletContainerVersion: jetty9Version,
        fullName: "Jetty $jetty9Version",
        servletContainerRunnerConfig: 'grettyRunnerJetty9',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "org.akhikhl.gretty:gretty-runner-jetty9:$grettyVersion"
        },
        servletApiDependencies: { project ->
          project.dependencies {
            providedCompile jetty9ServletApi
          }          
        }
      ],
      'tomcat7': [
        servletContainerType: 'tomcat',
        servletContainerVersion: tomcat7Version,
        fullName: "Tomcat $tomcat7Version",
        servletContainerRunnerConfig: 'grettyRunnerTomcat7',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "org.akhikhl.gretty:gretty-runner-tomcat:$grettyVersion", {
            exclude group: 'org.apache.tomcat.embed'
            exclude group: 'avax.servlet', module: 'javax.servlet-api'
          }
          project.dependencies.add servletContainerRunnerConfig, "org.apache.tomcat.embed:tomcat-embed-core:$tomcat7Version"
          project.dependencies.add servletContainerRunnerConfig, "org.apache.tomcat.embed:tomcat-embed-el:$tomcat7Version"
          project.dependencies.add servletContainerRunnerConfig, "org.apache.tomcat.embed:tomcat-embed-jasper:$tomcat7Version"
          project.dependencies.add servletContainerRunnerConfig, "org.apache.tomcat.embed:tomcat-embed-logging-juli:$tomcat7Version"
        },
        servletApiDependencies: { project ->
          project.dependencies {
            providedCompile tomcat7ServletApi
          }          
        }
      ],
      'tomcat8': [
        servletContainerType: 'tomcat',
        servletContainerVersion: tomcat8Version,
        fullName: "Tomcat $tomcat8Version",
        servletContainerRunnerConfig: 'grettyRunnerTomcat8',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "org.akhikhl.gretty:gretty-runner-tomcat:$grettyVersion", {
            exclude group: 'org.apache.tomcat.embed'
            exclude group: 'avax.servlet', module: 'javax.servlet-api'
          }
          project.dependencies.add servletContainerRunnerConfig, "org.apache.tomcat.embed:tomcat-embed-core:$tomcat8Version"
          project.dependencies.add servletContainerRunnerConfig, "org.apache.tomcat.embed:tomcat-embed-el:$tomcat8Version"
          project.dependencies.add servletContainerRunnerConfig, "org.apache.tomcat.embed:tomcat-embed-jasper:$tomcat8Version"
          project.dependencies.add servletContainerRunnerConfig, "org.apache.tomcat.embed:tomcat-embed-logging-juli:$tomcat8Version"
        },
        servletApiDependencies: { project ->
          project.dependencies {
            providedCompile tomcat8ServletApi
          }          
        }
      ]
    ]
  }

  static getConfig(servletContainer) {
    servletContainer = servletContainer ?: 'jetty9'
    def result = configs[servletContainer.toString()]
    if(!result)
      throw new GradleException("Unsupported servlet container: $servletContainer")
    result
  }

  static Map getConfigs() {
    configs.asImmutable()
  }
}

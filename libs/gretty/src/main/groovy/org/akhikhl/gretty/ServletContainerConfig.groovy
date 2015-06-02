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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class ServletContainerConfig {

  protected static final Logger log = LoggerFactory.getLogger(ServletContainerConfig)

	private static configs = createConfigs()

  private static void addRedirectFilter(Project project, String runnerConfig) {
    ProjectUtils.withOverlays(project).find { proj ->
      boolean alteredDependencies = false
      File webXmlFile = new File(ProjectUtils.getWebAppDir(proj), 'WEB-INF/web.xml')
      if(webXmlFile.exists()) {
        def webXml = new XmlSlurper().parse(webXmlFile)
        if(webXml.filter.find { it.'filter-class'.text() == 'org.akhikhl.gretty.RedirectFilter' }) {
          project.dependencies.add 'runtime', "org.akhikhl.gretty:gretty-filter:${project.ext.grettyVersion}", {
            exclude group: 'javax.servlet', module: 'servlet-api'
          }
          alteredDependencies = true
        }
      }
      alteredDependencies
    }
  }

  private static createConfigs() {
    String grettyVersion = Externalized.getString('grettyVersion')
    String jetty7Version = Externalized.getString('jetty7Version')
    String jetty7ServletApi = Externalized.getString('jetty7ServletApi')
    String jetty7ServletApiVersion = Externalized.getString('jetty7ServletApiVersion')
    String jetty8Version = Externalized.getString('jetty8Version')
    String jetty8ServletApi = Externalized.getString('jetty8ServletApi')
    String jetty8ServletApiVersion = Externalized.getString('jetty8ServletApiVersion')
    String jetty9Version = Externalized.getString('jetty9Version')
    String jetty9ServletApi = Externalized.getString('jetty9ServletApi')
    String jetty9ServletApiVersion = Externalized.getString('jetty9ServletApiVersion')
    String tomcat7Version = Externalized.getString('tomcat7Version')
    String tomcat7ServletApi = Externalized.getString('tomcat7ServletApi')
    String tomcat7ServletApiVersion = Externalized.getString('tomcat7ServletApiVersion')
    String tomcat8Version = Externalized.getString('tomcat8Version')
    String tomcat8ServletApi = Externalized.getString('tomcat8ServletApi')
    String tomcat8ServletApiVersion = Externalized.getString('tomcat8ServletApiVersion')
    [ 'jetty7': [
        servletContainerType: 'jetty',
        servletContainerVersion: jetty7Version,
        servletContainerDescription: "Jetty $jetty7Version",
        servletContainerRunnerConfig: 'grettyRunnerJetty7',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "org.akhikhl.gretty:gretty-runner-jetty7:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
        },
        servletApiVersion: jetty7ServletApiVersion,
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile jetty7ServletApi
          }
        }
      ],
      'jetty8': [
        servletContainerType: 'jetty',
        servletContainerVersion: jetty8Version,
        servletContainerDescription: "Jetty $jetty8Version",
        servletContainerRunnerConfig: 'grettyRunnerJetty8',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "org.akhikhl.gretty:gretty-runner-jetty8:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
        },
        servletApiVersion: jetty8ServletApiVersion,
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile jetty8ServletApi
          }
        }
      ],
      'jetty9': [
        servletContainerType: 'jetty',
        servletContainerVersion: jetty9Version,
        servletContainerDescription: "Jetty $jetty9Version",
        servletContainerRunnerConfig: 'grettyRunnerJetty9',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "org.akhikhl.gretty:gretty-runner-jetty9:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
        },
        servletApiVersion: jetty9ServletApiVersion,
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile jetty9ServletApi
            grettyProvidedCompile 'javax.websocket:javax.websocket-api:1.0'
          }
        }
      ],
      'tomcat7': [
        servletContainerType: 'tomcat',
        servletContainerVersion: tomcat7Version,
        servletContainerDescription: "Tomcat $tomcat7Version",
        servletContainerRunnerConfig: 'grettyRunnerTomcat7',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "org.akhikhl.gretty:gretty-runner-tomcat7:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
        },
        servletApiVersion: tomcat7ServletApiVersion,
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile tomcat7ServletApi
          }
        }
      ],
      'tomcat8': [
        servletContainerType: 'tomcat',
        servletContainerVersion: tomcat8Version,
        servletContainerDescription: "Tomcat $tomcat8Version",
        servletContainerRunnerConfig: 'grettyRunnerTomcat8',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "org.akhikhl.gretty:gretty-runner-tomcat8:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
        },
        servletApiVersion: tomcat8ServletApiVersion,
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile tomcat8ServletApi
          }
        }
      ]
    ]
  }

  static getConfig(servletContainer) {
    servletContainer = servletContainer ?: 'jetty9'
    def result = configs[servletContainer.toString()]
    if(!result)
      throw new Exception("Unsupported servlet container: $servletContainer")
    result
  }

  static Set getConfigNames() {
    configs.keySet().asImmutable()
  }

  static Map getConfigs() {
    configs.asImmutable()
  }

  static String getJettyCompatibleServletContainer(String servletContainer) {
    def config = getConfig(servletContainer)
    if(config.servletContainerType == 'jetty')
      return servletContainer
    def compatibleConfigEntry = getConfigs().find { name, c ->
      c.servletContainerType == 'jetty' && c.servletApiVersion == config.servletApiVersion
    }
    if(compatibleConfigEntry)
      return compatibleConfigEntry.key
    String defaultJettyServletContainer = 'jetty9'
    log.warn 'Cannot find jetty container with compatible servlet-api to {}, defaulting to {}', servletContainer, defaultJettyServletContainer
    defaultJettyServletContainer
  }

  static String getTomcatCompatibleServletContainer(String servletContainer) {
    def config = getConfig(servletContainer)
    if(config.servletContainerType == 'tomcat')
      return servletContainer
    def compatibleConfigEntry = getConfigs().find { name, c ->
      c.servletContainerType == 'tomcat' && c.servletApiVersion == config.servletApiVersion
    }
    if(compatibleConfigEntry)
      return compatibleConfigEntry.key
    String defaultJettyServletContainer = 'tomcat8'
    log.warn 'Cannot find tomcat container with compatible servlet-api to {}, defaulting to {}', servletContainer, defaultJettyServletContainer
    defaultJettyServletContainer
  }
}

/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.util.VersionNumber
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
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
          project.dependencies.add 'runtimeOnly', "org.gretty:gretty-filter:${project.ext.grettyVersion}", {
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
    def runnerGroup = "org.gretty"
    def configs = [:]
    configs['jetty10'] = [
      servletContainerType: 'jetty',
      servletContainerVersion: { project -> project.ext.jetty10Version },
      servletContainerDescription: { project -> "Jetty ${project.ext.jetty10Version}" },
      servletContainerRunnerConfig: 'grettyRunnerJetty10',
      servletContainerRunnerDependencies: { project ->
        project.dependencies.add servletContainerRunnerConfig, "${runnerGroup}:gretty-runner-jetty10:$grettyVersion"
        addRedirectFilter(project, servletContainerRunnerConfig)
        project.configurations[servletContainerRunnerConfig].resolutionStrategy {
          force "jakarta.servlet:jakarta.servlet-api:${project.ext.jetty10ServletApiVersion}"
          def jettyVversion = project.ext.jetty10Version
          force "org.eclipse.jetty:jetty-server:$jettVersion"
          force "org.eclipse.jetty:jetty-servlet:$jettyVersion"
          force "org.eclipse.jetty:jetty-webapp:$jettyVersion"
          force "org.eclipse.jetty:jetty-security:$jettyVersion"
          force "org.eclipse.jetty:apache-jsp:$jettyVersion"
          force "org.eclipse.jetty:jetty-annotations:$jettyVersion"
          force "org.eclipse.jetty:jetty-plus:$jettyVersion"
          force "org.eclipse.jetty.websocket:javax-websocket-server-impl:$jettyVersion"
          def asm_version = project.ext.asmVersion
          force "org.ow2.asm:asm:$asm_version"
          force "org.ow2.asm:asm-commons:$asm_version"
        }
      },
      servletApiVersion: { project -> project.ext.jetty10ServletApiVersion },
      servletApiDependencies: { project ->
        project.dependencies {
          grettyProvidedCompile "jakarta.servlet:jakarta.servlet-api:${project.ext.jetty10ServletApiVersion}"
          grettyProvidedCompile 'jakarta.websocket:jakarta.websocket-api:2.0.0-M1'
        }
      }
    ]

    configs['tomcat10'] = [
      servletContainerType: 'tomcat',
      servletContainerVersion: { project -> project.ext.tomcat10Version },
      servletContainerDescription: { project -> "Tomcat ${project.ext.tomcat10Version}" },
      servletContainerRunnerConfig: 'grettyRunnerTomcat10',
      servletContainerRunnerDependencies: { project ->
        project.dependencies.add servletContainerRunnerConfig, "${runnerGroup}:gretty-runner-tomcat10:$grettyVersion"
        addRedirectFilter(project, servletContainerRunnerConfig)
        project.configurations[servletContainerRunnerConfig].resolutionStrategy {
          force "jakarta.servlet:jakarta.servlet-api:${project.ext.tomcat10ServletApiVersion}"
          def tomcatVersion = project.ext.tomcat10Version
          force "org.apache.tomcat.embed:tomcat-embed-core:$tomcatVersion"
          force "org.apache.tomcat.embed:tomcat-embed-el:$tomcatVersion"
          force "org.apache.tomcat.embed:tomcat-embed-jasper:$tomcatVersion"
          force "org.apache.tomcat.embed:tomcat-embed-websocket:$tomcatVersion"
        }
      },
      servletApiVersion: { project -> project.ext.tomcat10ServletApiVersion },
      servletApiDependencies: { project ->
        project.dependencies {
          grettyProvidedCompile "jakarta.servlet:jakarta.servlet-api:${project.ext.tomcat10ServletApiVersion}"
          grettyProvidedCompile 'jakarta.websocket:jakarta.websocket-api:2.0.0-M1'
        }
      }
    ]
    return configs
  }

  static getConfig(servletContainer) {
    servletContainer = servletContainer ?: 'tomcat10'
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

  static String getJettyCompatibleServletContainer(Project project, String servletContainer) {
    def config = getConfig(servletContainer)
    if(config.servletContainerType == 'jetty')
      return servletContainer
    def compatibleConfigEntry = getConfigs().find { name, c ->
      c.servletContainerType == 'jetty' && c.servletApiVersion(project) == config.servletApiVersion(project)
    }
    if(compatibleConfigEntry)
      return compatibleConfigEntry.key
    String defaultJettyServletContainer = 'jetty10'
    log.warn 'Cannot find jetty container with compatible servlet-api to {}, defaulting to {}', servletContainer, defaultJettyServletContainer
    defaultJettyServletContainer
  }

  static String getTomcatCompatibleServletContainer(Project project, String servletContainer) {
    def config = getConfig(servletContainer)
    if(config.servletContainerType == 'tomcat')
      return servletContainer
    def compatibleConfigEntry = getConfigs().find { name, c ->
      c.servletContainerType == 'tomcat' && c.servletApiVersion(project) == config.servletApiVersion(project)
    }
    if(compatibleConfigEntry)
      return compatibleConfigEntry.key
    String defaultJettyServletContainer = 'tomcat10'
    log.warn 'Cannot find tomcat container with compatible servlet-api to {}, defaulting to {}', servletContainer, defaultJettyServletContainer
    defaultJettyServletContainer
  }
}

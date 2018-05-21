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
    def configs = [ 'jetty7': [
        servletContainerType: 'jetty',
        servletContainerVersion: { project -> project.ext.jetty7Version },
        servletContainerDescription: { project -> "Jetty ${project.ext.jetty7Version}" },
        servletContainerRunnerConfig: 'grettyRunnerJetty7',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "${runnerGroup}:gretty-runner-jetty7:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
          project.configurations[servletContainerRunnerConfig].resolutionStrategy {
            force "javax.servlet:servlet-api:$project.ext.jetty7ServletApiVersion"
            def jetty7_version = project.ext.jetty7Version
            force "org.eclipse.jetty:jetty-server:$jetty7_version"
            force "org.eclipse.jetty:jetty-servlet:$jetty7_version"
            force "org.eclipse.jetty:jetty-webapp:$jetty7_version"
            force "org.eclipse.jetty:jetty-security:$jetty7_version"
            force "org.eclipse.jetty:jetty-jsp:$jetty7_version"
            force "org.eclipse.jetty:jetty-plus:$jetty7_version"
          }
        },
        servletApiVersion: { project -> project.ext.jetty7ServletApiVersion },
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile "javax.servlet:servlet-api:$project.ext.jetty7ServletApiVersion"
          }
        }
      ],
      'jetty8': [
        servletContainerType: 'jetty',
        servletContainerVersion: { project -> project.ext.jetty8Version },
        servletContainerDescription: { project -> "Jetty ${project.ext.jetty8Version}" },
        servletContainerRunnerConfig: 'grettyRunnerJetty8',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "${runnerGroup}:gretty-runner-jetty8:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
          project.configurations[servletContainerRunnerConfig].resolutionStrategy {
            force "javax.servlet:javax.servlet-api:${project.ext.jetty8ServletApiVersion}"
            def jetty8_version = project.ext.jetty8Version
            force "org.eclipse.jetty:jetty-server:$jetty8_version"
            force "org.eclipse.jetty:jetty-servlet:$jetty8_version"
            force "org.eclipse.jetty:jetty-webapp:$jetty8_version"
            force "org.eclipse.jetty:jetty-security:$jetty8_version"
            force "org.eclipse.jetty:jetty-jsp:$jetty8_version"
            force "org.eclipse.jetty:jetty-annotations:$jetty8_version"
            force "org.eclipse.jetty:jetty-plus:$jetty8_version"
          }
        },
        servletApiVersion: { project -> project.ext.jetty8ServletApiVersion },
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile "javax.servlet:javax.servlet-api:${project.ext.jetty8ServletApiVersion}"
          }
        }
      ],
      'jetty9': [
        servletContainerType: 'jetty',
        servletContainerVersion: { project -> project.ext.jetty9Version },
        servletContainerDescription: { project -> "Jetty ${project.ext.jetty9Version}" },
        servletContainerRunnerConfig: 'grettyRunnerJetty9',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "${runnerGroup}:gretty-runner-jetty9:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
          project.configurations[servletContainerRunnerConfig].resolutionStrategy {
            force "javax.servlet:javax.servlet-api:${project.ext.jetty9ServletApiVersion}"
            def jetty9_version = project.ext.jetty9Version
            force "org.eclipse.jetty:jetty-server:$jetty9_version"
            force "org.eclipse.jetty:jetty-servlet:$jetty9_version"
            force "org.eclipse.jetty:jetty-webapp:$jetty9_version"
            force "org.eclipse.jetty:jetty-security:$jetty9_version"
            force "org.eclipse.jetty:jetty-jsp:$jetty9_version"
            force "org.eclipse.jetty:jetty-annotations:$jetty9_version"
            force "org.eclipse.jetty:jetty-plus:$jetty9_version"
            force "org.eclipse.jetty.websocket:javax-websocket-server-impl:$jetty9_version"
            def asm_version = project.ext.asmVersion
            force "org.ow2.asm:asm:$asm_version"
            force "org.ow2.asm:asm-commons:$asm_version"
          }
        },
        servletApiVersion: { project -> project.ext.jetty9ServletApiVersion },
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile "javax.servlet:javax.servlet-api:${project.ext.jetty9ServletApiVersion}"
            grettyProvidedCompile 'javax.websocket:javax.websocket-api:1.0'
          }
        }
      ],
      'tomcat7': [
        servletContainerType: 'tomcat',
        servletContainerVersion: { project -> project.ext.tomcat7Version },
        servletContainerDescription: { project -> "Tomcat ${project.ext.tomcat7Version}" },
        servletContainerRunnerConfig: 'grettyRunnerTomcat7',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "${runnerGroup}:gretty-runner-tomcat7:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
          project.configurations[servletContainerRunnerConfig].resolutionStrategy {
            force "javax.servlet:javax.servlet-api:${project.ext.tomcat7ServletApiVersion}"
            def tomcat7_version = project.ext.tomcat7Version
            force "org.apache.tomcat.embed:tomcat-embed-core:$tomcat7_version"
            force "org.apache.tomcat.embed:tomcat-embed-logging-log4j:$tomcat7_version"
            force "org.apache.tomcat.embed:tomcat-embed-el:$tomcat7_version"
            force "org.apache.tomcat.embed:tomcat-embed-jasper:$tomcat7_version"
            force "org.apache.tomcat.embed:tomcat-embed-websocket:$tomcat7_version"
          }
        },
        servletApiVersion: { project -> project.ext.tomcat7ServletApiVersion },
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile "javax.servlet:javax.servlet-api:${project.ext.tomcat7ServletApiVersion}"
          }
        }
      ],
      'tomcat8': [
        servletContainerType: 'tomcat',
        servletContainerVersion: { project -> project.ext.tomcat8Version },
        servletContainerDescription: { project -> "Tomcat ${project.ext.tomcat8Version}" },
        servletContainerRunnerConfig: 'grettyRunnerTomcat8',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "${runnerGroup}:gretty-runner-tomcat8:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
          project.configurations[servletContainerRunnerConfig].resolutionStrategy {
            force "javax.servlet:javax.servlet-api:${project.ext.tomcat8ServletApiVersion}"
            def tomcat8_version = project.ext.tomcat8Version
            force "org.apache.tomcat.embed:tomcat-embed-core:$tomcat8_version"
            force "org.apache.tomcat.embed:tomcat-embed-el:$tomcat8_version"
            force "org.apache.tomcat.embed:tomcat-embed-jasper:$tomcat8_version"
            force "org.apache.tomcat.embed:tomcat-embed-websocket:$tomcat8_version"
            // this fixes incorrect dependency of tomcat-8.0.9 on ecj-4.4RC4
            if(tomcat8_version == '8.0.9')
              force 'org.eclipse.jdt.core.compiler:ecj:4.4'
          }
        },
        servletApiVersion: { project -> project.ext.tomcat8ServletApiVersion },
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile "javax.servlet:javax.servlet-api:${project.ext.tomcat8ServletApiVersion}"
          }
        }
      ],
      'tomcat85': [
        servletContainerType: 'tomcat',
        servletContainerVersion: { project -> project.ext.tomcat85Version },
        servletContainerDescription: { project -> "Tomcat ${project.ext.tomcat85Version}" },
        servletContainerRunnerConfig: 'grettyRunnerTomcat85',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "${runnerGroup}:gretty-runner-tomcat85:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
          project.configurations[servletContainerRunnerConfig].resolutionStrategy {
            force "javax.servlet:javax.servlet-api:${project.ext.tomcat85ServletApiVersion}"
            def tomcat85_version = project.ext.tomcat85Version
            force "org.apache.tomcat.embed:tomcat-embed-core:$tomcat85_version"
            force "org.apache.tomcat.embed:tomcat-embed-el:$tomcat85_version"
            force "org.apache.tomcat.embed:tomcat-embed-jasper:$tomcat85_version"
            if (VersionNumber.parse(tomcat85_version) <= VersionNumber.parse('8.5.2'))
              force "org.apache.tomcat.embed:tomcat-embed-logging-log4j:$tomcat85_version"
            force "org.apache.tomcat.embed:tomcat-embed-websocket:$tomcat85_version"
          }
        },
        servletApiVersion: { project -> project.ext.tomcat85ServletApiVersion },
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile "javax.servlet:javax.servlet-api:${project.ext.tomcat85ServletApiVersion}"
          }
        }
      ],
    ]
    if (JavaVersion.current().isJava8Compatible()) {
      configs['jetty9.3'] = [
        servletContainerType: 'jetty',
        servletContainerVersion: { project -> project.ext.jetty93Version },
        servletContainerDescription: { project -> "Jetty ${project.ext.jetty93Version}" },
        servletContainerRunnerConfig: 'grettyRunnerJetty93',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "${runnerGroup}:gretty-runner-jetty93:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
          project.configurations[servletContainerRunnerConfig].resolutionStrategy {
            force "javax.servlet:javax.servlet-api:${project.ext.jetty9ServletApiVersion}"
            def jetty93_version = project.ext.jetty93Version
            force "org.eclipse.jetty:jetty-server:$jetty93_version"
            force "org.eclipse.jetty:jetty-servlet:$jetty93_version"
            force "org.eclipse.jetty:jetty-webapp:$jetty93_version"
            force "org.eclipse.jetty:jetty-security:$jetty93_version"
            force "org.eclipse.jetty:apache-jsp:$jetty93_version"
            force "org.eclipse.jetty:jetty-annotations:$jetty93_version"
            force "org.eclipse.jetty:jetty-plus:$jetty93_version"
            force "org.eclipse.jetty.websocket:javax-websocket-server-impl:$jetty93_version"
            def asm_version = project.ext.asmVersion
            force "org.ow2.asm:asm:$asm_version"
            force "org.ow2.asm:asm-commons:$asm_version"
          }
        },
        servletApiVersion: { project -> project.ext.jetty9ServletApiVersion },
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile "javax.servlet:javax.servlet-api:${project.ext.jetty9ServletApiVersion}"
            grettyProvidedCompile 'javax.websocket:javax.websocket-api:1.0'
          }
        }
      ]
      configs['jetty9.4'] = [
          servletContainerType: 'jetty',
          servletContainerVersion: { project -> project.ext.jetty94Version },
          servletContainerDescription: { project -> "Jetty ${project.ext.jetty94Version}" },
          servletContainerRunnerConfig: 'grettyRunnerJetty94',
          servletContainerRunnerDependencies: { project ->
            project.dependencies.add servletContainerRunnerConfig, "${runnerGroup}:gretty-runner-jetty94:$grettyVersion"
            addRedirectFilter(project, servletContainerRunnerConfig)
            project.configurations[servletContainerRunnerConfig].resolutionStrategy {
              force "javax.servlet:javax.servlet-api:${project.ext.jetty9ServletApiVersion}"
              def jetty94_version = project.ext.jetty94Version
              force "org.eclipse.jetty:jetty-server:$jetty94_version"
              force "org.eclipse.jetty:jetty-servlet:$jetty94_version"
              force "org.eclipse.jetty:jetty-webapp:$jetty94_version"
              force "org.eclipse.jetty:jetty-security:$jetty94_version"
              force "org.eclipse.jetty:apache-jsp:$jetty94_version"
              force "org.eclipse.jetty:jetty-annotations:$jetty94_version"
              force "org.eclipse.jetty:jetty-plus:$jetty94_version"
              force "org.eclipse.jetty.websocket:javax-websocket-server-impl:$jetty94_version"
              def asm_version = project.ext.asmVersion
              force "org.ow2.asm:asm:$asm_version"
              force "org.ow2.asm:asm-commons:$asm_version"
            }
          },
          servletApiVersion: { project -> project.ext.jetty9ServletApiVersion },
          servletApiDependencies: { project ->
            project.dependencies {
              grettyProvidedCompile "javax.servlet:javax.servlet-api:${project.ext.jetty9ServletApiVersion}"
              grettyProvidedCompile 'javax.websocket:javax.websocket-api:1.0'
            }
          }
      ]
      configs['tomcat9'] = [
        servletContainerType: 'tomcat',
        servletContainerVersion: { project -> project.ext.tomcat9Version },
        servletContainerDescription: { project -> "Tomcat ${project.ext.tomcat9Version}" },
        servletContainerRunnerConfig: 'grettyRunnerTomcat9',
        servletContainerRunnerDependencies: { project ->
          project.dependencies.add servletContainerRunnerConfig, "${runnerGroup}:gretty-runner-tomcat9:$grettyVersion"
          addRedirectFilter(project, servletContainerRunnerConfig)
          project.configurations[servletContainerRunnerConfig].resolutionStrategy {
            force "javax.servlet:javax.servlet-api:${project.ext.tomcat9ServletApiVersion}"
            def tomcat9_version = project.ext.tomcat9Version
            force "org.apache.tomcat.embed:tomcat-embed-core:$tomcat9_version"
            force "org.apache.tomcat.embed:tomcat-embed-el:$tomcat9_version"
            force "org.apache.tomcat.embed:tomcat-embed-jasper:$tomcat9_version"
            force "org.apache.tomcat.embed:tomcat-embed-websocket:$tomcat9_version"
          }
        },
        servletApiVersion: { project -> project.ext.tomcat9ServletApiVersion },
        servletApiDependencies: { project ->
          project.dependencies {
            grettyProvidedCompile "javax.servlet:javax.servlet-api:${project.ext.tomcat9ServletApiVersion}"
          }
        }
      ]
    }
    return configs
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

  static String getJettyCompatibleServletContainer(Project project, String servletContainer) {
    def config = getConfig(servletContainer)
    if(config.servletContainerType == 'jetty')
      return servletContainer
    def compatibleConfigEntry = getConfigs().find { name, c ->
      c.servletContainerType == 'jetty' && c.servletApiVersion(project) == config.servletApiVersion(project)
    }
    if(compatibleConfigEntry)
      return compatibleConfigEntry.key
    String defaultJettyServletContainer = 'jetty9'
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
    String defaultJettyServletContainer = 'tomcat8'
    log.warn 'Cannot find tomcat container with compatible servlet-api to {}, defaulting to {}', servletContainer, defaultJettyServletContainer
    defaultJettyServletContainer
  }
}

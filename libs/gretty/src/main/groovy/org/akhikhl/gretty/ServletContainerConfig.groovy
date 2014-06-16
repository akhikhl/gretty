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
    String jetty8Version = Externalized.getString('jetty8Version')
    String jetty9Version = Externalized.getString('jetty9Version')
    [ 'jetty7': [
        servletContainerType: 'jetty',
        servletContainerVersion: jetty7Version,
        fullName: "Jetty $jetty7Version",
        grettyServletContainerRunnerConfig: 'grettyRunnerJetty7',
        grettyServletContainerRunnerGAV: "org.akhikhl.gretty:gretty-runner-jetty7:$grettyVersion",
        servletApiGAV: 'javax.servlet:servlet-api:2.5'
      ],
      'jetty8': [
        servletContainerType: 'jetty',
        servletContainerVersion: jetty8Version,
        fullName: "Jetty $jetty8Version",
        grettyServletContainerRunnerConfig: 'grettyRunnerJetty8',
        grettyServletContainerRunnerGAV: "org.akhikhl.gretty:gretty-runner-jetty8:$grettyVersion",
        servletApiGAV: 'javax.servlet:javax.servlet-api:3.0.1'
      ],
      'jetty9': [
        servletContainerType: 'jetty',
        servletContainerVersion: jetty9Version,
        fullName: "Jetty $jetty9Version",
        grettyServletContainerRunnerConfig: 'grettyRunnerJetty9',
        grettyServletContainerRunnerGAV: "org.akhikhl.gretty:gretty-runner-jetty9:$grettyVersion",
        servletApiGAV: 'javax.servlet:javax.servlet-api:3.1.0'
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

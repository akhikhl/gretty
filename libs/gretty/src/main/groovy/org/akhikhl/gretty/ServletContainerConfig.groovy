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
        servletContainerVersion: jetty7Version,
        jettyVersion: jetty7Version,
        fullName: "Jetty $jetty7Version",
        grettyHelperConfig: 'grettyHelper7',
        grettyHelperGAV: "org.akhikhl.gretty:gretty7-helper:$grettyVersion",
        grettyUtilConfig: 'grettyUtil7',
        grettyUtilPackage: 'org.akhikhl.gretty.util7',
        servletApiGAV: 'javax.servlet:servlet-api:2.5'
      ],
      'jetty8': [
        servletContainerVersion: jetty8Version,
        jettyVersion: jetty8Version,
        fullName: "Jetty $jetty8Version",
        grettyHelperConfig: 'grettyHelper8',
        grettyHelperGAV: "org.akhikhl.gretty:gretty8-helper:$grettyVersion",
        grettyUtilConfig: 'grettyUtil8',
        grettyUtilPackage: 'org.akhikhl.gretty.util8',
        servletApiGAV: 'javax.servlet:javax.servlet-api:3.0.1'
      ],
      'jetty9': [
        servletContainerVersion: jetty9Version,
        jettyVersion: jetty9Version,
        fullName: "Jetty $jetty9Version",
        grettyHelperConfig: 'grettyHelper9',
        grettyHelperGAV: "org.akhikhl.gretty:gretty9-helper:$grettyVersion",
        grettyUtilConfig: 'grettyUtil9',
        grettyUtilPackage: 'org.akhikhl.gretty.util9',
        servletApiGAV: 'javax.servlet:javax.servlet-api:3.1.0'
      ]
    ]
  }
  
  static getConfig(servletContainer) {
    servletContainer = servletContainer ?: 'jetty9'
    def result = configs[servletContainer.toString()]
    if(!result)
      throw new GradleException("Unsupported servlet container: $servletContainer")
  }
  
  static Map getConfigs() {
    configs.asImmutable()
  }
}

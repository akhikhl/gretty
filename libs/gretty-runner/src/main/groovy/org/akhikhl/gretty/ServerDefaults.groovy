/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
class ServerDefaults {

  static final String defaultHost = '0.0.0.0'
  static final int defaultHttpPort = 8080
  static final int defaultHttpsPort = 8443

  static Map getEffectiveParams(Map params) {
    Map result = [:] + params
    if(!result.host)
      result.host = defaultHost
    if(!result.httpPort)
      result.httpPort = defaultHttpPort
    if(!result.httpsPort)
      result.httpsPort = defaultHttpsPort
    result
  }

  static Map getRestrictedEffectiveParams(Map params) {
    Map result = [:]
    result.host = params.host ?: defaultHost
    result.httpPort = params.httpPort ?: defaultHttpPort
    result.httpsPort = params.httpsPort ?: defaultHttpsPort
    result
  }
}


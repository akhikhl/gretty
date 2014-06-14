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
class JettyVersionResolver {
  
  private static ResourceBundle messages
  
	static String resolve(int simpleVersion) {
    if(messages == null)
      messages = ResourceBundle.getBundle('org.akhikhl.gretty.Messages', Locale.ENGLISH, this.getClass().getClassLoader())
    switch(simpleVersion) {
      case 7:
        return messages.getString('jetty7Version')
      case 8:
        return messages.getString('jetty7Version')
      case 9:
        return messages.getString('jetty7Version')
      default:
        throwUnsupportedJettyVersion(simpleVersion)
    }
  }
  
  static void throwUnsupportedJettyVersion(jettyVersion) {
    throw new GradleException("Unsupported jetty version: $jettyVersion (supported versions: 7, 8, 9)")
  }
}


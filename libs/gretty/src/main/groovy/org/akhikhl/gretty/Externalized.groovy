/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
class Externalized {
  
  private static ResourceBundle resources
  
	static String getString(String key) {
    if(resources == null)
      resources = ResourceBundle.getBundle('org.akhikhl.gretty.Externalized', Locale.ENGLISH, Externalized.getClassLoader())
    resources.getString(key)
  }
}


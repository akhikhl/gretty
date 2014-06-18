/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.apache.commons.configuration.PropertiesConfiguration

/**
 *
 * @author akhikhl
 */
class Externalized {

  private static PropertiesConfiguration config

	static String getString(String key) {
    if(config == null) {
      config = new PropertiesConfiguration()
      URLConnection resConn = Externalized.getResource('Externalized.properties').openConnection()
      // this fixes exceptions when reloading classes in running application
      resConn.setUseCaches(false)
      resConn.getInputStream().withStream {
        config.load(it)
      }
    }
    config.getString(key)
  }
}

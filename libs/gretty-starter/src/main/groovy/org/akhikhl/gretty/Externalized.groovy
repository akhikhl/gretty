/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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

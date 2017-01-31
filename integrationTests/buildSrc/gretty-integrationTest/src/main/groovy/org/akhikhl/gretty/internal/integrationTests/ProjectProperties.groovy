package org.akhikhl.gretty.internal.integrationTests

import org.apache.commons.configuration.PropertiesConfiguration

class ProjectProperties {

  private static PropertiesConfiguration config

  static synchronized String getString(String key) {
    if(config == null) {
      config = new PropertiesConfiguration()
      URLConnection resConn = ProjectProperties.getResource('project.properties').openConnection()
      // this fixes exceptions when reloading classes in running application
      resConn.setUseCaches(false)
      resConn.getInputStream().withStream {
        config.load(it)
      }
    }
    config.getString(key)
  }
}

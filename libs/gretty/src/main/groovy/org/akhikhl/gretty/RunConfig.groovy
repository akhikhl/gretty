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
interface RunConfig {
  
  String getServletContainer()
  
  boolean getManagedClassReload()

  ServerConfig getServerConfig()

  Iterable<WebAppConfig> getWebAppConfigs()
}


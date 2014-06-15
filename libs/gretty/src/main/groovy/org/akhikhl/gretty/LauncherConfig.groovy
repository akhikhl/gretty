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
interface LauncherConfig {

  boolean getDebug()
  
  boolean getIntegrationTest()
  
  boolean getInteractive()
  
  def getJacocoConfig()
  
  boolean getManagedClassReload()

  ServerConfig getServerConfig()
  
  String getServletContainer()
  
  String getStopTaskName()

  Iterable<WebAppConfig> getWebAppConfigs()
}


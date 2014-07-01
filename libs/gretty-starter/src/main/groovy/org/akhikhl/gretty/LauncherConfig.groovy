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

  boolean getInteractive()

  boolean getManagedClassReload()

  ServerConfig getServerConfig()

  String getStopCommand()

  WebAppClassPathResolver getWebAppClassPathResolver()

  Iterable<WebAppConfig> getWebAppConfigs()
}


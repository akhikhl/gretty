/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
interface LauncherConfig {

  boolean getDebug()

  int getDebugPort()

  boolean getDebugSuspend()

  boolean getInteractive()

  boolean getManagedClassReload()

  ServerConfig getServerConfig()

  String getStopCommand()

  File getBaseDir()

  boolean getProductMode()

  WebAppClassPathResolver getWebAppClassPathResolver()

  Iterable<WebAppConfig> getWebAppConfigs()
}


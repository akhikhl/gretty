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
interface LauncherConfig {

  boolean getDebug()

  boolean getInteractive()

  boolean getManagedClassReload()

  ServerConfig getServerConfig()

  String getStopCommand()

  File getBaseDir()

  WebAppClassPathResolver getWebAppClassPathResolver()

  Iterable<WebAppConfig> getWebAppConfigs()
}


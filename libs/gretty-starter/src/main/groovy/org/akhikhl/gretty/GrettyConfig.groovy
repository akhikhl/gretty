/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

class GrettyConfig {

  @Delegate
  protected ServerConfig serverConfig = new ServerConfig()

  @Delegate
  protected WebAppConfig webAppConfig = new WebAppConfig()
}

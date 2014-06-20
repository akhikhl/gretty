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
class ServerManagerFactory {

  static ServerManager createServerManager() {
    def TomcatConfigurer = Class.forName('org.akhikhl.gretty.TomcatConfigurerImpl', true, ServerManagerFactory.classLoader)
    new TomcatServerManager(TomcatConfigurer.newInstance())
  }
}


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
    def JettyConfigurer = Class.forName('org.akhikhl.gretty.JettyConfigurerImpl', true, ServerManagerFactory.classLoader)
    new JettyServerManager(JettyConfigurer.newInstance())
  }
}


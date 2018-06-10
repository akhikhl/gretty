/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class ServerManagerFactory {

  static ServerManager createServerManager() {
    def JettyConfigurer = Class.forName('org.akhikhl.gretty.JettyConfigurerImpl', true, ServerManagerFactory.classLoader)
    new JettyServerManager(JettyConfigurer.newInstance())
  }
}


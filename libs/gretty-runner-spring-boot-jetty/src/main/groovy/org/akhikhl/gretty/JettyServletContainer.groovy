/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.eclipse.jetty.server.Server
import org.springframework.boot.context.embedded.EmbeddedServletContainerException
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainer

/**
 *
 * @author akhikhl
 */
class JettyServletContainer extends JettyEmbeddedServletContainer {

  private final Closure onStart

  JettyServletContainer(Server server, Closure onStart) {
    super(server, true)
    this.onStart = onStart
  }

	@Override
	public void start() throws EmbeddedServletContainerException {
    super.start()
    if(onStart != null)
      onStart()
  }
}


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
class TomcatStartTask extends AppStartTask {

  @Override
  protected String getCompatibleServletContainer(String servletContainer) {
    ServletContainerConfig.getTomcatCompatibleServletContainer(servletContainer)
  }
}

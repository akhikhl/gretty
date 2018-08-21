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
import org.apache.catalina.Service
import org.apache.catalina.startup.Tomcat
/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class TomcatConfigurerImpl extends AbstractTomcatConfigurerImpl {
  @Override
  void setService(Tomcat tomcat, Service service) {
    tomcat.service = service
  }

  @Override
  void setEngine(Tomcat tomcat, Service service) {
    tomcat.engine = service.getContainer()
  }
}

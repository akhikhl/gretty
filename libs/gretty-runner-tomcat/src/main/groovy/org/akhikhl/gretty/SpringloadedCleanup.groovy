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
import org.apache.catalina.Lifecycle
import org.apache.catalina.LifecycleEvent
import org.apache.catalina.LifecycleListener
import org.apache.catalina.core.StandardContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class SpringloadedCleanup implements LifecycleListener {

  protected final Logger log

  SpringloadedCleanup() {
    log = LoggerFactory.getLogger(this.getClass())
  }

  @Override
  public void lifecycleEvent(LifecycleEvent event) {
    if(event.getType() == Lifecycle.BEFORE_STOP_EVENT)
      cleanup(event.getLifecycle())
  }

  protected void cleanup(StandardContext context) {
    def TypeRegistry
    try {
      TypeRegistry = Class.forName('org.springsource.loaded.TypeRegistry', true, this.class.getClassLoader())
    } catch(ClassNotFoundException e) {
      // springloaded not present, just ignore
      return
    }
    ClassLoader classLoader = context.getLoader().getClassLoader()
    while(classLoader != null) {
      def typeRegistry = TypeRegistry.getTypeRegistryFor(classLoader)
      if(typeRegistry != null && typeRegistry.@fsWatcher != null) {
        log.info 'springloaded shutdown: {}', typeRegistry.@fsWatcher.@thread
        typeRegistry.@fsWatcher.shutdown()
        typeRegistry.@fsWatcher.@thread.join()
      }
      classLoader = classLoader.getParent()
    }
  }
}


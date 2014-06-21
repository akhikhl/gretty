/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

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


/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * Gradle task for starting jetty synchronously
 *
 * @author akhikhl
 */
class GrettySyncStartTask extends GrettyStartTask {

  ExecutorService executorService
  int syncPort

  @Override
  void action() {
    Future started = ServiceControl.readMessage(executorService, syncPort)
    Thread.start {
      run()
    }
    started.get()
  }
}

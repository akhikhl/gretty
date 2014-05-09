/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * Gradle task for synchronous control over jetty
 *
 * @author akhikhl
 */
class GrettySyncServiceTask extends GrettyServiceTask {

  ExecutorService executorService
  int syncPort

  @Override
  void action() {
    Future stopped = ServiceControl.readMessage(executorService, syncPort)
    ServiceControl.send(servicePort, command)
    stopped.get()
  }

  @Override
  void setupProperties() {
    super.setupProperties()
    executorService = executorService ?: Executors.newSingleThreadExecutor()
    requiredProperty syncPort
  }
}


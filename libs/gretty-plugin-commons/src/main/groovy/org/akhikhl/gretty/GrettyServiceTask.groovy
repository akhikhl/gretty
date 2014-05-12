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
import org.gradle.api.DefaultTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Gradle task for control over jetty
 *
 * @author akhikhl
 */
class GrettyServiceTask extends GrettyBaseTask {

  private static Logger log = LoggerFactory.getLogger(GrettyServiceTask)

  Integer servicePort
  Integer statusPort
  ExecutorService executorService
  String command

  @Override
  void action() {
    log.debug 'Sending command: {}', command
    ServiceControl.send(servicePort, command)
  }

  @Override
  protected void setupProperties() {
    if(servicePort == null) servicePort = project.gretty.servicePort
    if(statusPort == null) statusPort = project.gretty.statusPort
    if(executorService == null) executorService = project.ext.executorService ?: Executors.newSingleThreadExecutor()
    requiredProperty 'command'
  }
}

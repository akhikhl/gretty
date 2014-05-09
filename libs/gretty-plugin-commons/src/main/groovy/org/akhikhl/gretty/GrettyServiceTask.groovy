/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.DefaultTask

/**
 * Gradle task for control over jetty
 *
 * @author akhikhl
 */
class GrettyServiceTask extends GrettyBaseTask {

  int servicePort
  String command

  @Override
  void action() {
    ServiceControl.send(servicePort, command)
  }

  @Override
  void setupProperties() {
    servicePort = servicePort ?: project.gretty.servicePort
    requiredProperty 'command'
  }
}

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty


/**
 * Gradle task for control over jetty
 *
 * @author akhikhl
 */
class GrettyServiceTask {

  String command

  GrettyServiceTask() {
    doLast {
      action()
    }
  }

  void action() {
    ServiceControl.send(project.gretty.servicePort, command)
  }
}

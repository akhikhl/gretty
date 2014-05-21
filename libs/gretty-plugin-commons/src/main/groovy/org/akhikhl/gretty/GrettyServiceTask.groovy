/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 * This class is deprecated, please use JettyStopTask and JettyRestartTask instead.
 *
 * @author akhikhl
 */
@Deprecated
class GrettyServiceTask extends JettyServiceTask {

  String command

  String getCommand() {
    command
  }
}

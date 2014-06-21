/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 * This class is deprecated, please use AppStopTask and AppRestartTask instead.
 *
 * @author akhikhl
 */
@Deprecated
class GrettyServiceTask extends AppServiceTask {

  String command

  String getCommand() {
    command
  }
}

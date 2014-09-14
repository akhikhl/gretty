/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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

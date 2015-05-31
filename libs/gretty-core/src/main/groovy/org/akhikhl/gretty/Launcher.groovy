/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
interface Launcher {

  void afterLaunch()

  void beforeLaunch()

  void dispose()

  void launch()
  
  Thread launchThread()
}

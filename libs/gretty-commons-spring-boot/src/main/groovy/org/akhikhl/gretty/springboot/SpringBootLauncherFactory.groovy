/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import org.akhikhl.gretty.Launcher
import org.akhikhl.gretty.LauncherFactory

/**
 *
 * @author akhikhl
 */
class SpringBootLauncherFactory implements LauncherFactory {
  
	Launcher createLauncher() {
    new SpringBootLauncher()
  }
}


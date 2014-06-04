/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import org.akhikhl.gretty.Runner
import org.akhikhl.gretty.RunnerFactory

/**
 *
 * @author akhikhl
 */
class SpringBootRunnerFactory implements RunnerFactory {
  
	Runner createRunner() {
    new SpringBootRunner()
  }
}


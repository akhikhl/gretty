package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@CompileStatic(TypeCheckingMode.SKIP)
class PortUtils {

  // attention: this constant must always have the same value as ServerConfig.RANDOM_FREE_PORT
  static final int RANDOM_FREE_PORT = -1
}

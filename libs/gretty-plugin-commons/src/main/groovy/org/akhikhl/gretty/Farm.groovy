 /*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class Farm {

  @Delegate
  protected ServerConfig serverConfig = new ServerConfig()

  // key is project path or war path, value is options
  Map<String, Map> webapps = [:]

  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webapps[w] = options
  }
}

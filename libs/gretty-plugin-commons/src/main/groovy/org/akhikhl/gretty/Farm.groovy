 /*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
final class Farm {

  @Delegate
  protected ServerConfig serverConfig = new ServerConfig()

  List webapps = []

  void webapp(w) {
    webapps.add(w)
  }
}

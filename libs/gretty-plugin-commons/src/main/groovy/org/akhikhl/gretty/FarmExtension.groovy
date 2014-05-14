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
class FarmExtension {

  void webapp(w) {
    def f = ext.farms['']
    if(f == null)
      f = ext.farms[''] = new Farm()
    f.webapps.add(w)
  }
}


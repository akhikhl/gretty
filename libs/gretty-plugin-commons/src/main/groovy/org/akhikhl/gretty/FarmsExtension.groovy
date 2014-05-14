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
class FarmsExtension {

  void farm(String name = null, Closure closure) {
    if(name == null)
      name = ''
    def f = ext.farms[name]
    if(f == null)
      f = ext.farms[name] = new Farm()
    closure.delegate = f
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure()
  }
}


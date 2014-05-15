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
class Farms {

  protected Map farmsMap = [:]

  void farm(String name = null, Closure closure) {
    if(name == null)
      name = ''
    def f = farmsMap[name]
    if(f == null)
      f = farmsMap[name] = new Farm()
    closure.delegate = f
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure()
  }
}

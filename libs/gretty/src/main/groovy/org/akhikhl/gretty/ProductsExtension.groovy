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
 *
 * @author akhikhl
 */
class ProductsExtension {

  Map productsMap = [:]

  void product(String name = null, Closure closure) {
    if(name == null)
      name = ''
    def p = productsMap[name]
    if(p == null)
      p = productsMap[name] = new ProductExtension()
    closure.delegate = p
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure()
  }
}


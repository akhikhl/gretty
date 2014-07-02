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


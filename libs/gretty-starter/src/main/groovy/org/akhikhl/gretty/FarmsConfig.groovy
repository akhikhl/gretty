/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
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
class FarmsConfig {

  protected final Map farmsMap_ = [:]

  FarmConfig createFarm() {
    new FarmConfig()
  }

  void farm(String name = null, Closure closure) {
    if(name == null)
      name = ''
    def f = farmsMap_[name]
    if(f == null)
      f = farmsMap_[name] = createFarm()
    closure.delegate = f
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure()
  }

  Map getFarmsMap() {
    farmsMap_.asImmutable()
  }
}

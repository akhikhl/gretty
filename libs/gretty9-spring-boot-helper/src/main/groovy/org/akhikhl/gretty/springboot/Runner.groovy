/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import groovy.json.JsonSlurper

/**
 *
 * @author akhikhl
 */
class Runner {

  static void main(String[] args) {
    assert args.length != 0
    Map params = new JsonSlurper().parseText(args[0])
    def MainAppClass = Class.forName(params.mainClass, true, Runner.classLoader)
    assert MainAppClass != null
    MainAppClass.main(args.drop(1))
  }	
}


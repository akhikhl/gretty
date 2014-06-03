/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonSlurper

/**
 *
 * @author akhikhl
 */
class SpringBootRunner {

  static void main(String[] args) {
    assert args.length != 0
    Map params = new JsonSlurper().parseText(args[0])
    def MainAppClass = Class.forName(params.mainClass, true, SpringBootRunner.classLoader)
    assert MainAppClass != null
    MainAppClass.main(args.drop(1))
  }	
}


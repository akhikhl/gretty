/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import groovy.json.JsonSlurper
import org.akhikhl.gretty.RunnerBase
import org.akhikhl.gretty.ServerManager

/**
 *
 * @author akhikhl
 */
class Runner extends RunnerBase {

  static void main(String[] args) {
    assert args.length != 0
    Map params = new JsonSlurper().parseText(args[0])
    new Runner(params).run()
  }

  private Runner(Map params) {
    super(params)
  }
  
  @Override
  protected ServerManager createServerManager() {
    new SpringBootServerManager()
  }  
}

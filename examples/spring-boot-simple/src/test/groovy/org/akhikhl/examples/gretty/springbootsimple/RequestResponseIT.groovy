/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty.springbootsimple

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  private static String baseURI

  void setupSpec() {
    baseURI = System.getProperty('gretty.baseURI')
  }

  def 'should get expected static page'() {
  when:
    go baseURI
  then:
    $('h1').text() == 'Hello, world!'
    $('p', 0).text() == /This is static HTML page./
  }

  def 'should get response from spring controller'() {
  when:
    go baseURI
    $('#sendRequest').click()
    waitFor { $("p.hide#result").size() == 0 }
  then:
    $('#result').text() == 'Got from server: ' + new Date().format('EEE, d MMM yyyy')
  }
}

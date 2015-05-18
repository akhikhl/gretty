/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty.springbootapp

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

  def 'should get response from spring-boot-webservice1'() {
    if(!System.getProperty('gretty.farm'))
      return
  when:
    go baseURI
    $('#sendRequest1').click()
    waitFor { $("p.hide#result1").size() == 0 }
  then:
    $('#result1').text() == 'Got from spring-boot-webservice1: ' + new Date().format('EEE, d MMM yyyy')
  }

  def 'should get response from spring-boot-webservice2'() {
    if(!System.getProperty('gretty.farm'))
      return
  when:
    go baseURI
    $('#sendRequest2').click()
    waitFor { $("p.hide#result2").size() == 0 }
  then:
    $('#result2').text() == 'Got from spring-boot-webservice2: ' + new Date().format('EEE, d MMM yyyy')
  }

  def 'should get response from jee web-service'() {
    if(!System.getProperty('gretty.farm'))
      return
  when:
    go baseURI
    $('#sendRequest3').click()
    waitFor { $("p.hide#result3").size() == 0 }
  then:
    $('#result3').text() == 'Got from jee-webservice: ' + new Date().format('EEE, d MMM yyyy')
  }
}

package org.akhikhl.examples.gretty.mywebapp

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

  def 'should get ajax response from MyWebApp'() {
  when:
    go baseURI
    $('#sendRequestMyWebApp').click()
  then:
    $('#result').text() == 'Got from MyWebApp: Hello, world!'
  }

  def 'should get ajax response from MyWebService'() {
    if(!System.getProperty('gretty.farm'))
      return
  when:
    go baseURI
    $('#sendRequestMyWebService').click()
  then:
    $('#result').text() == 'Got from MyWebService: ' + new Date().format('EEE, d MMM yyyy')
  }  
}


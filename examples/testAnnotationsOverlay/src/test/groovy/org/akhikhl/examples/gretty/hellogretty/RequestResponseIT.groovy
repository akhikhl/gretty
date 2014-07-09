package org.akhikhl.examples.gretty.hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  private static String baseURI

  void setupSpec() {
    baseURI = System.getProperty('gretty.baseURI')
  }

  def 'should get expected response from the server'() {
  when:
    go "${baseURI}/annotations/whatever"
  then:
    $('h1').text() == /Hello, world!/
    $('p strong').text() == /The template for this page comes from webapp overlay./
  }
}

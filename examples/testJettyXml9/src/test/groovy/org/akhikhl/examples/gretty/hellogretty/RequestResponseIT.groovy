package org.akhikhl.examples.gretty.hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  def 'should get expected response from the server'() {
  when:
    go 'http://localhost:7777/testJettyXml9'
  then:
    $('h1').text() == /Hello, Gretty! It's fine weather today, isn't it?/
  }
}

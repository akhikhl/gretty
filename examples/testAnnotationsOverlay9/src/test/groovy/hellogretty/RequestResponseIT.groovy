package hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  def 'should get expected response from the server'() {
  when:
    go 'http://localhost:8080/testAnnotationsOverlay9/annotations/whatever'
  then:
    $('h1').text() == /Hello, Gretty! This is a message from overlayed web-application!/
  }
}

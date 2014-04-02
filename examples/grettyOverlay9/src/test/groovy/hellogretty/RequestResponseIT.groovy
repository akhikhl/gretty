package hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  def 'should get expected response from servlet'() {
  when:
    go 'http://localhost:8080/grettyOverlay9/hello'
  then:
    $('h1').text() == /Hello, Gretty! This is a message from overlayed web-application!/
  }

  def 'should get expected static page'() {
  when:
    go 'http://localhost:8080/grettyOverlay9/test.html'
  then:
    $('div').text() == 'TODO write content'
  }
}

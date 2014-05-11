package hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  def 'should get expected response from servlet'() {
  when:
    go 'http://localhost:8080/grettyStartTask/hello'
  then:
    $('h1').text() == /Welcome to Gretty!/
  }

  def 'should get expected static page'() {
  when:
    go 'http://localhost:8080/grettyStartTask/test.html'
  then:
    $('h1').text() == /Welcome to Gretty!/
  }
}

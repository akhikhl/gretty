package integrationtest

import spock.lang.Specification
import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  def 'should get expected response from the server'() {
  when:
    go 'http://localhost:8080/integrationTest'
  then:
    $('div.greeting').text() == 'Hello, integration test!'
  }
}
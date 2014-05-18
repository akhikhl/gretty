package mywebapp

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  def 'should get expected static page'() {
  when:
    go 'http://localhost:8080/MyWebApp/index.html'
  then:
    $('h1').text() == 'Hello, world!'
    $('p', 0).text() == /This is static HTML page./
  }
}

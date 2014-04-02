package hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  def 'should get expected response from servlet'() {
  when:
    go 'http://localhost:8080/helloGretty9/hello'
  then:
    $('h1').text() == /Hello, Gretty! It's fine weather today, isn't it?/
  }

  def 'should get expected static page'() {
  when:
    go 'http://localhost:8080/helloGretty9/test.html'
  then:
    $('div').text() == 'TODO write content'
  }
}

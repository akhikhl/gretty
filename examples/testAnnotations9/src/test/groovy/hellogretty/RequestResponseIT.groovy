package hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  private static int grettyPort

  void setupSpec() {
    grettyPort = System.getProperty('gretty.port') as int
  }

  def 'should get expected response from the server'() {
  when:
    go "http://localhost:${grettyPort}/testAnnotations9/annotations/whatever"
  then:
    $('h1').text() == /Hello, Gretty! It's fine weather today, isn't it?/
  }
}

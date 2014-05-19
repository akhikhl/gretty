package hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  private static int grettyPort

  void setupSpec() {
    grettyPort = System.getProperty('gretty.port') as int
  }

  def 'should get expected response from the server'() {
  when:
    go "http://localhost:${grettyPort}/testAnnotationsOverlay8/annotations/whatever"
  then:
    $('h1').text() == /Hello, Gretty! This is a message from overlayed web-application!/
  }
}

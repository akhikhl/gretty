package hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  private static int grettyPort
  private static String contextPath

  void setupSpec() {
    grettyPort = System.getProperty('gretty.port') as int
    contextPath = System.getProperty('gretty.contextPath')
  }

  def 'should get expected response from the server'() {
  when:
    go "http://localhost:${grettyPort}/abc"
  then:
    // contextPath is overridden in jetty-env.xml
    contextPath != '/abc'
    $('h1').text() == /Hello, Gretty! It's fine weather today, isn't it?/
  }
}

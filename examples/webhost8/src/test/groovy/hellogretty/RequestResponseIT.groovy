package hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  private static int grettyPort

  void setupSpec() {
    grettyPort = System.getProperty('gretty.port') as int
  }

  def 'should get response from servlet declared in META-INF/web-fragment.xml'() {
  when:
    go "http://localhost:${grettyPort}/webhost8/exampleservlet"
  then:
    $('h1').text() == /Hello, this is webfragment.ExampleServlet/
  }

  def 'should see static files in META-INF/resources'() {
  when:
    go "http://localhost:${grettyPort}/webhost8/page1.htm"
  then:
    $('h1').text() == /page loaded from webfragment/
  }
}

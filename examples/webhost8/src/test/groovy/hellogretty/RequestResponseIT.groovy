package hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  private static String baseURI

  void setupSpec() {
    baseURI = System.getProperty('gretty.baseURI')
  }

  def 'should get response from servlet declared in META-INF/web-fragment.xml'() {
  when:
    go "${baseURI}/exampleservlet"
  then:
    $('h1').text() == /Hello, this is webfragment.ExampleServlet/
  }

  def 'should see static files in META-INF/resources'() {
  when:
    go "${baseURI}/page1.htm"
  then:
    $('h1').text() == /page loaded from webfragment/
  }
}

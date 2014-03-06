package hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  def 'should get response from servlet declared in META-INF/web-fragment.xml'() {
  when:
    go 'http://localhost:8080/webhost9/exampleservlet'
  then:
    $('h1').text() == /Hello, this is webfragment.ExampleServlet/
  }

  def 'should see static files in META-INF/resources'() {
  when:
    go 'http://localhost:8080/webhost9/page1.htm'
  then:
    $('h1').text() == /page loaded from webfragment/
  }
}

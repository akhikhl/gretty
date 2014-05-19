package mywebapp

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  private static int grettyPort

  void setupSpec() {
    grettyPort = System.getProperty('gretty.port') as int
  }

  def 'should get expected static page'() {
  when:
    go "http://localhost:${grettyPort}/MyWebApp/index.html"
  then:
    $('h1').text() == 'Hello, world!'
    $('p', 0).text() == /This is static HTML page./
  }

  def 'should get response from MyWebService'() {
  when:
    go "http://localhost:${grettyPort}/MyWebApp/index.html"
    $('#sendRequest').click()
  then:
    $('#result').text() == 'Got from server: ' + new Date().format('EEE, d MMM yyyy')
  }
}

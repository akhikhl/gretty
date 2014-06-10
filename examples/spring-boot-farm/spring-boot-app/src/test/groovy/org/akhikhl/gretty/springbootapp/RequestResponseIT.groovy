package springbootsimple

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  private static String baseURI

  void setupSpec() {
    baseURI = System.getProperty('gretty.baseURI')
  }

  def 'should get expected static page'() {
  when:
    go baseURI
  then:
    $('h1').text() == 'Hello, world!'
    $('p', 0).text() == /This is static HTML page./
  }

  def 'should get response from spring-boot web-service'() {
    if(!System.getProperty('gretty.farm'))
      return
  when:
    go baseURI
    $('#sendRequest1').click()
    waitFor { $("p.hide#result1").size() == 0 }
  then:
    $('#result1').text() == 'Got from server: ' + new Date().format('EEE, d MMM yyyy')
  }

  def 'should get response from jee web-service'() {
    if(!System.getProperty('gretty.farm'))
      return
  when:
    go baseURI
    $('#sendRequest2').click()
    waitFor { $("p.hide#result2").size() == 0 }
  then:
    $('#result2').text() == 'Got from server: ' + new Date().format('EEE, d MMM yyyy')
  }
}

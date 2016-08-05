package org.akhikhl.examples.gretty.springbootwebsocket;

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  private static String host
  private static int port
  private static String contextPath

  void setupSpec() {
    host = System.getProperty('gretty.host')
    port = System.getProperty('gretty.port') as int
    contextPath = System.getProperty('gretty.contextPath')
  }

  def 'should get expected static page'() {
  setup:
    go "http://${host}:${port}${contextPath}/index.html"
  expect:
    $('h1').text() == 'spring-boot websocket'
  }

  def 'should get expected response from websocket server'() {
  setup:
    go "http://${host}:${port}${contextPath}/index.html"
  expect:
    $('#connect').click()
    waitFor { $('#conversationDiv').displayed }
    $('#name').value('John')
    $('#sendName').click()
    waitFor { $('#response p').text() }
    $('#response p').text() == 'Hello, John!'
  }
}

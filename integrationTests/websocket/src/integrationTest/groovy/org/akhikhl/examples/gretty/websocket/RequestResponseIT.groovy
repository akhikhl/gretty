/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty.websocket

import geb.Browser
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
    $('h1').text() == 'Websocket chat'
  }

  def 'should send chat messages'() {
  setup:
    String url = "http://${host}:${port}${contextPath}/index.html"
    go url
    def otherBrowser
    withNewWindow({ $('#openNewChat').click() }, close: false) {
      $('#username').value('a')
      $('#message').value('xxx')
      $('#btnSend').click()
      otherBrowser = getCurrentWindow()
    }
    $('#username').value('b')
    $('#message').value('yyy')
    $('#btnSend').click()
    // Thread.sleep(200)
    withWindow(otherBrowser) {
      waitFor { $('#chat-content').value() }
      assert $('#chat-content').value().contains('yyy')
    }
    waitFor { $('#chat-content').value() }
    assert $('#chat-content').value().contains('xxx')
  }
}

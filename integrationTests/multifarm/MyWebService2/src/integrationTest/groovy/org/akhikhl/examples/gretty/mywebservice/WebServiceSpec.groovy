/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty.mywebservice

import org.akhikhl.gretty.GrettyAjaxSpec
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class WebServiceSpec extends GrettyAjaxSpec {

  def 'should handle requests'() {
  when:
    def result = conn.request(POST, JSON) {
      uri.path = "${contextPath}/getdate"
      response.success = { resp, json ->
        json.date
      }
      response.failure = { resp ->
        resp.statusLine.statusCode
      }
    }
  then:
    result == new Date().format('EEE, d MMM yyyy')
  }
}

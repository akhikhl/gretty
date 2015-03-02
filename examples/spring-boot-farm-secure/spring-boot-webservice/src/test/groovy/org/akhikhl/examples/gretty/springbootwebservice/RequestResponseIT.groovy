/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty.springbootwebservice

import org.akhikhl.gretty.GrettyAjaxSpec
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class RequestResponseIT extends GrettyAjaxSpec {

  def 'should reject unauthorized requests'() {
  when:
    def result = https.request(POST, JSON) {
      uri.path = "${contextPath}/mycontroller/getdate"
      response.success = { resp, json ->
        json.date
      }
      response.failure = { resp ->
        resp.statusLine.statusCode
      }
    }
  then:
    result == 401 // unauthorized
  }

  def 'should reject requests with invalid credentials'() {
  when:
    https.auth.basic 'bogus', 'blabla'
    def result = https.request(POST, JSON) {
      uri.path = "${contextPath}/mycontroller/getdate"
      response.success = { resp, json ->
        json.date
      }
      response.failure = { resp ->
        resp.statusLine.statusCode
      }
    }
  then:
    result == 401 // unauthorized
  }

  def 'should handle authorized requests'() {
  when:
    https.auth.basic 'test', 'test123'
    def result = https.request(POST, JSON) {
      uri.path = "${contextPath}/mycontroller/getdate"
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


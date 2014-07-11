package org.akhikhl.examples.gretty.springbootwebservice

import org.akhikhl.gretty.GrettyAjaxSpec
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class RequestResponseIT extends GrettyAjaxSpec {

  def 'should handle requests'() {
  when:
    def result = conn.request(POST, JSON) {
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


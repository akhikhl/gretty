/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty.hellojersey

import org.akhikhl.gretty.GrettyAjaxSpec
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import static javax.servlet.http.HttpServletResponse.*

class RequestResponseSpec extends GrettyAjaxSpec {

  def 'should get expected response from Jersey web-service'() {
  when:
    def result = [:]
    conn.request GET, TEXT, {
      uri.path = "$contextPath/testresource"
      response.success = response.failure = { resp, data ->
        result.statusCode = resp.statusLine.statusCode
        if(data instanceof InputStreamReader)
          result.data = data.text
        else
          result.data = data
      }
    }
  then:
    result.statusCode == SC_OK
    result.data == 'Hello World!'
  }
}

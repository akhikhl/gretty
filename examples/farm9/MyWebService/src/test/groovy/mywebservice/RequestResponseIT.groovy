package mywebservice

import spock.lang.Specification
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class RequestResponseIT extends Specification {

  def http

  def setup() {
    http = new HTTPBuilder('http://localhost:8080')
  }

  def 'should get expected response'() {
  when:
    def result = http.request(POST, JSON) {
      uri.path = '/MyWebService/getdate'
      response.success = { resp, json ->
        json.date
      }
    }
  then:
    result == new Date().format('EEE, d MMM yyyy')
  }
}

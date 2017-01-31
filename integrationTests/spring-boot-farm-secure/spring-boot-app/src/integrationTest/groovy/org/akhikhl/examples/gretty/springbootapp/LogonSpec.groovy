/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty.springbootapp

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

  private static String baseURI

  void setupSpec() {
    baseURI = System.getProperty('gretty.preferredBaseURI')
  }

  def 'should reject invalid credentials'() {
  when:
    go baseURI
  then: 'base URI should redirect to login page'
    $('h1.login-title').text() == 'Log in'
  when:
    $('form.form-signin input[name=j_username]').value('abc')
    $('form.form-signin input[name=j_password]').value('def')
    $('form.form-signin button[type=submit]').click()
  then: 'login should fail'
    waitFor { $('h1.login-title').text() == 'Login failed' }
  }

  def 'should accept valid credentials and interact with web-services'() {
    if(!System.getProperty('gretty.farm'))
      return
  when:
    go baseURI
  then: 'base URI should redirect to login page'
    $('h1.login-title').text() == 'Log in'
  when:
    $('form.form-signin input[name=j_username]').value('test')
    $('form.form-signin input[name=j_password]').value('test123')
    $('form.form-signin button[type=submit]').click()
  then: 'login should succeed'
    waitFor { $('h1').text() == 'Hello, world!' }
    $('p', 0).text() == 'This is static HTML page.'
  when:
    $('#sendRequest1').click()
    waitFor { $("p.hide#result1").size() == 0 }
  then: 'ajax call to spring-boot web-service should succeed with the same credentials'
    $('#result1').text() == 'Got from server: ' + new Date().format('EEE, d MMM yyyy')
  when:
    $('#sendRequest2').click()
    waitFor { $("p.hide#result2").size() == 0 }
  then:
    waitFor { $('#result2').text() == 'Got from server: ' + new Date().format('EEE, d MMM yyyy') }
  }
}

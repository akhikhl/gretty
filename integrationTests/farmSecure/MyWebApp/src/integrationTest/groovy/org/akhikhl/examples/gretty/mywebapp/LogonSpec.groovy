/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty.mywebapp

import geb.spock.GebReportingSpec

class LogonSpec extends GebReportingSpec {

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

  def 'should accept valid credentials'() {
  when:
    go baseURI
  then: 'base URI should redirect to login page'
    $('h1.login-title').text() == 'Log in'
  when:
    $('form.form-signin input[name=j_username]').value('test')
    $('form.form-signin input[name=j_password]').value('test123')
    $('form.form-signin button[type=submit]').click()
  then: 'login should succeed'
    waitFor{ $('h1').text() == 'Hello, world!' }
    $('p', 0).text() == 'This is static HTML page.'
    $('#result').text() == ''
  when:
    $('#sendRequest').click()
  then: 'ajax call should succeed with the same credentials'
    waitFor { $('#result').text() == 'Got from server: ' + new Date().format('EEE, d MMM yyyy') }
  }
}

/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovyx.net.http.HTTPBuilder
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import org.apache.http.client.HttpClient
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.ssl.TrustStrategy
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 * @author akhikhl
 */
class GrettyAjaxSpec extends Specification {

  @Shared String host
  @Shared String contextPath
  @Shared String httpPort
  @Shared String httpBaseURI
  @Shared String httpsPort
  @Shared String httpsBaseURI
  @Shared String preferredProtocol
  @Shared String preferredBaseURI

  void setupSpec() {
    host = System.getProperty('gretty.host')
    contextPath = System.getProperty('gretty.contextPath')
    httpPort = System.getProperty('gretty.httpPort')
    httpBaseURI = System.getProperty('gretty.httpBaseURI')
    httpsPort = System.getProperty('gretty.httpsPort')
    httpsBaseURI = System.getProperty('gretty.httpsBaseURI')
    preferredProtocol = System.getProperty('gretty.preferredProtocol')
    preferredBaseURI = System.getProperty('gretty.preferredBaseURI')
  }

  protected HTTPBuilder _http
  protected HTTPBuilder _https

  void setup() {
    _http = null
    _https = null
  }

  HTTPBuilder getHttp() {
    if(_http == null) {
      if(httpBaseURI == null)
        throw new Exception('HTTP connection is not supported by server')
      _http = new HTTPBuilder(httpBaseURI)
    }
    _http
  }

  HTTPBuilder getHttps() {
    if(_https == null) {
      if(httpsBaseURI == null)
        throw new Exception('HTTPS connection is not supported by server')
      _https = new HTTPBuilder(httpsBaseURI)
      TrustStrategy ts = new TrustStrategy() {
        @Override
        public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
          return true
        }
      }
      def sf = new SSLSocketFactory(ts, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
      def scheme = new Scheme('https', httpsPort as int, sf)
      _https.getClient().getConnectionManager().getSchemeRegistry().register(scheme)
    }
    _https
  }

  HTTPBuilder getConn() {
    preferredProtocol == 'https' ? getHttps() : getHttp()
  }
}

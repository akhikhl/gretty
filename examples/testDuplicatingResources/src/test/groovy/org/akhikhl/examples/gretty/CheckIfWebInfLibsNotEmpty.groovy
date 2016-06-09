/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty.hellogretty

import geb.spock.GebReportingSpec

class RequestResponseIT extends GebReportingSpec {

    private static String baseURI

    void setupSpec() {
        baseURI = System.getProperty('gretty.baseURI')
    }

    def 'should get expected static page'() {
        when:
        go "${baseURI}"
        then:
        $('p').text() == 'list size: 1'
    }
}

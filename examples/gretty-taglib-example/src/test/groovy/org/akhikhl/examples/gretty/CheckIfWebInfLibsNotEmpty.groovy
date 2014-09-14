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
        $('p').text() == '/WEB-INF/lib is not empty'
    }
}

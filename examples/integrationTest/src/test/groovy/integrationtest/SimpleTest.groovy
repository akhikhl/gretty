package integrationtest

import spock.lang.Specification

class SimpleTest extends Specification {

  def 'test that 2x2 equals 4'() {
    expect:
    2 * 2 == 4
  }
}
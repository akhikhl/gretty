/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
class JarSkipPatterns {

  private static final List defaultPatterns = [
    'ant-*.jar',
    'aspectj*.jar',
    'commons-beanutils*.jar',
    'commons-cli-*.jar',
    'commons-codec*.jar',
    'commons-collections*.jar',
    'commons-dbcp*.jar',
    'commons-digester*.jar',
    'commons-fileupload*.jar',
    'commons-httpclient*.jar',
    'commons-io*.jar',
    'commons-lang*.jar',
    'commons-logging*.jar',
    'commons-math*.jar',
    'commons-pool*.jar',
    'geronimo-spec-jaxrpc*.jar',
    'h2*.jar',
    'hamcrest*.jar',
    'hibernate*.jar',
    'jmx*.jar',
    'jmx-tools-*.jar',
    'jta*.jar',
    'junit-*.jar',
    'httpclient*.jar',
    'log4j-*.jar',
    'mail*.jar',
    'org.hamcrest*.jar',
    'slf4j*.jar',
    'tomcat-embed-core-*.jar',
    'tomcat-embed-logging-*.jar',
    'tomcat-jdbc-*.jar',
    'tomcat-juli-*.jar',
    'tools.jar',
    'wsdl4j*.jar',
    'xercesImpl-*.jar',
    'xmlParserAPIs-*.jar',
    'xml-apis-*.jar',
    // gretty-specific
    'commons-configuration*.jar',
    'ecj-*.jar',
    'gretty-runner*.jar',
    'groovy-all*.jar',
    'groovy-backports-*.jar',
    'javax.servlet*.jar',
    'jcl-over-slf4j*.jar',
    'jul-to-slf4j*.jar',
    'logback*.jar',
    'springloaded*.jar',
    'sysout-over-slf4j*.jar',
    'tomcat-embed*.jar',
    'velocity-*.jar' ]

  private final Set<String> patterns = new LinkedHashSet<String>()

  JarSkipPatterns() {
    for(String pattern in defaultPatterns)
      add(pattern)
  }

  JarSkipPatterns(String patterns) {
    for(String pattern in defaultPatterns)
      add(pattern)
    addPatterns(patterns)
  }

  void add(String pattern) {
    assert pattern != null
    this.patterns.add(pattern)
  }
  
  void addPatterns(String patterns) {
    if(patterns != null) {
      StringTokenizer tokenizer = new StringTokenizer(patterns, ',')
      while (tokenizer.hasMoreElements())
        this.patterns.add(tokenizer.nextToken())
    }
  }

  public Set<String> asSet() {
    return Collections.unmodifiableSet(this.patterns)
  }
}

/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.io.File
import java.io.IOException
import java.net.JarURLConnection
import java.util.Collections
import java.util.HashSet
import java.util.LinkedHashSet
import java.util.Set
import java.util.StringTokenizer
import javax.servlet.ServletContext
import org.apache.catalina.core.StandardContext
import org.apache.tomcat.JarScanner
import org.apache.tomcat.JarScannerCallback
import org.apache.tomcat.util.file.Matcher
import org.apache.tomcat.util.scan.StandardJarScanner
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Note that SkipPatternJarScannerBase does not override scan method.
 * This is related to incompatible changes in JarScanner interface between tomcat7 and tomcat8.
 * The override is done in derived classes, which are compiled against concrete versions of tomcat.
 *
 * @author akhikhl
 */
public class SkipPatternJarScannerBase extends StandardJarScanner {

  private static final Logger log = LoggerFactory.getLogger(SkipPatternJarScannerBase.class)

	protected final JarScanner jarScanner
	protected final SkipPattern pattern

  boolean debug = false

	SkipPatternJarScannerBase(JarScanner jarScanner, String pattern) {
		assert jarScanner != null : 'JarScanner must not be null'
		this.jarScanner = jarScanner
		this.pattern = new SkipPattern(pattern)
	}

  protected JarScannerCallback augmentCallback(final JarScannerCallback callback) {

    if(!debug)
      return callback

    return new JarScannerCallback() {

      public void scan(JarURLConnection urlConn) throws IOException {
        log.warn('jarScannerCallback.scan {}', urlConn)
        callback.scan(urlConn)
      }

      public void scan(File file) throws IOException {
        log.warn('jarScannerCallback.scan {}', file)
        callback.scan(file)
      }
    }
  }

  protected Set<String> augmentJarsToSkip(Set<String> jarsToSkip) {
    jarsToSkip = jarsToSkip == null ? new HashSet<String>() : new HashSet<String>(jarsToSkip)
    jarsToSkip.addAll(this.pattern.asSet())
    jarsToSkip = Collections.unmodifiableSet(jarsToSkip)
    return jarsToSkip
  }

  protected boolean checkJar(String path) {
    !Matcher.matchName(this.pattern.asSet(), path.substring(path.lastIndexOf('/')+1))
  }

	protected static final class SkipPattern {

    private static final List defaultPatterns = [
			'ant-*.jar',
			'aspectj*.jar',
			'commons-beanutils*.jar',
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
      'javax.servlet*.jar',
      'jcl-over-slf4j*.jar',
      'jul-to-slf4j*.jar',
      'logback*.jar',
      'springloaded*.jar',
      'sysout-over-slf4j*.jar',
      'tomcat-embed*.jar' ]

		private final Set<String> patterns = new LinkedHashSet<String>()

		protected SkipPattern(String patterns) {

      for(String pattern in defaultPatterns)
        add(pattern)

      if(patterns != null) {
        StringTokenizer tokenizer = new StringTokenizer(patterns, ',')
        while (tokenizer.hasMoreElements()) {
          add(tokenizer.nextToken())
        }
      }
		}

		protected void add(String patterns) {
			assert patterns != null : 'Patterns must not be null'
			if (patterns.length() > 0 && !patterns.trim().startsWith(',')) {
				this.patterns.add(',')
			}
			this.patterns.add(patterns)
		}

		public Set<String> asSet() {
			return Collections.unmodifiableSet(this.patterns)
		}
	}
}

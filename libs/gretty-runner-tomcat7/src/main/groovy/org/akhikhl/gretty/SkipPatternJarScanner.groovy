/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import javax.servlet.ServletContext
import org.apache.tomcat.JarScanner
import org.apache.tomcat.JarScannerCallback
import org.apache.tomcat.util.scan.StandardJarScanner
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class SkipPatternJarScanner extends StandardJarScanner {

  private static final Logger log = LoggerFactory.getLogger(SkipPatternJarScanner)

	protected final JarScanner jarScanner
	protected final JarSkipPatterns skipPatterns

	SkipPatternJarScanner(JarScanner jarScanner, JarSkipPatterns skipPatterns) {
		assert jarScanner != null
		this.jarScanner = jarScanner
		this.skipPatterns = skipPatterns
	}

  protected JarScannerCallback augmentCallback(final JarScannerCallback callback) {

    if(!log.isDebugEnabled())
      return callback

    return new JarScannerCallback() {

      public void scan(JarURLConnection urlConn) throws IOException {
        log.debug('jarScannerCallback.scan {}', urlConn)
        callback.scan(urlConn)
      }

      public void scan(File file) throws IOException {
        log.debug('jarScannerCallback.scan {}', file)
        callback.scan(file)
      }
    }
  }

  protected Set<String> augmentJarsToSkip(Set<String> jarsToSkip) {
    jarsToSkip = jarsToSkip == null ? new HashSet() : new HashSet(jarsToSkip)
    jarsToSkip += skipPatterns.asSet()
    jarsToSkip = Collections.unmodifiableSet(jarsToSkip)
    return jarsToSkip
  }

	@Override
	public void scan(ServletContext context, ClassLoader classloader, JarScannerCallback callback, Set<String> jarsToSkip) {
		this.jarScanner.scan(context, classloader, augmentCallback(callback), augmentJarsToSkip(jarsToSkip))
	}
}


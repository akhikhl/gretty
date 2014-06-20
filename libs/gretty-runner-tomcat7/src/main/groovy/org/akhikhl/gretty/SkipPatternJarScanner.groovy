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

/**
 *
 * @author akhikhl
 */
class SkipPatternJarScanner extends SkipPatternJarScannerBase {

	SkipPatternJarScanner(JarScanner jarScanner, String pattern) {
    super(jarScanner, pattern)
  }

	@Override
	public void scan(ServletContext context, ClassLoader classloader, final JarScannerCallback callback, Set<String> jarsToSkip) {
		this.jarScanner.scan(context, classloader, augmentCallback(callback), augmentJarsToSkip(jarsToSkip))
	}
}


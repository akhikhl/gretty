/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import javax.servlet.ServletContext
import org.apache.tomcat.JarScanFilter
import org.apache.tomcat.JarScanner
import org.apache.tomcat.JarScannerCallback
import org.apache.tomcat.util.scan.StandardJarScanFilter

import org.apache.tomcat.JarScanType

/**
 *
 * @author akhikhl
 */
class SkipPatternJarScanner extends SkipPatternJarScannerBase {

	SkipPatternJarScanner(JarScanner jarScanner, String pattern) {
    super(jarScanner, pattern)
    setJarScanFilter(new StandardJarScanFilter())
  }

  @Override
  public void setJarScanFilter(JarScanFilter additionalScanFilter) {
    super.setJarScanFilter(new JarScanFilter() {
      boolean check(JarScanType jarScanType, String jarName) {
        checkJar(jarName) && (additionalScanFilter == null || additionalScanFilter.check(jarScanType, jarName))
      }
    })
  }

	@Override
  public void scan(JarScanType scanType, ServletContext context, JarScannerCallback callback) {
		jarScanner.scan(scanType, context, augmentCallback(callback))
	}
}

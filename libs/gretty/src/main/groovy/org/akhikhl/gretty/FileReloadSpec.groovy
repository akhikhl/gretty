 /*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.transform.ToString

/**
 *
 * @author akhikhl
 */
@ToString
class FileReloadSpec {
  File baseDir
  def pattern
  def excludesPattern
}


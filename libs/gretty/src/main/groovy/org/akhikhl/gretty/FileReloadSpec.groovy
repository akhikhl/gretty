/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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


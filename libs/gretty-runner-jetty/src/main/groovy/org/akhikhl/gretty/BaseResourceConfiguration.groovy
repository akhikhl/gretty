/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

interface BaseResourceConfiguration {

  void addBaseResourceListener(Closure closure)

  void setExtraResourceBases(List extraResourceBases)
}


/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

interface BaseResourceConfiguration {

  void addBaseResourceListener(Closure closure)

  void setExtraResourceBases(List extraResourceBases)
}


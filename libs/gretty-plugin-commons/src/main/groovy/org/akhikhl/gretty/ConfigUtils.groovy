/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
class ConfigUtils {

  public static complementProperties(Object dst, Object... srcs) {
    List dstProps = dst.metaClass.properties.collect { it.name } - ['class']
    for(def src in srcs)
      for(String propName in dstProps)
        if(dst[propName] == null && src.metaClass.properties.find { it.name == propName } && src[propName] != null) {
          dst[propName] = src[propName]
        }
    dst
  }
}

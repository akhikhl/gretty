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
    for(def src in srcs) {
      if(src instanceof Map) {
        for(String propName in dstProps)
          if(dst[propName] == null && src[propName] != null)
            dst[propName] = src[propName]
      }
      else {
        for(String propName in dstProps)
          if(dst[propName] == null && src.metaClass.properties.find { it.name == propName } && src[propName] != null)
            dst[propName] = src[propName]
      }
    }
    dst
  }

  public static resolveClosures(Object obj) {
    if(obj == null)
      return
    def props
    if(obj instanceof Map)
      props = [] + obj.keySet()
    else
      props = obj.metaClass.properties.collect { it.name } - ['class']
    for(String propName in props)
      if(obj[propName] instanceof Closure)
        obj[propName] = obj[propName]()
  }
}

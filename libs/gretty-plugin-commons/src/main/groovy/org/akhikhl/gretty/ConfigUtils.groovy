/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.GradleException

/**
 *
 * @author akhikhl
 */
class ConfigUtils {

  static complementProperties(Object dst, Iterable srcs) {
    complementProperties_(dst, srcs)
  }

  static complementProperties(Object dst, Object... srcs) {
    complementProperties_(dst, srcs)
  }

  private static complementProperties_(Object dst, Object srcs) {
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

  static void requireAnyProperty(Object obj, String... propNames) {
    if(obj instanceof Map) {
      for(String propName in propNames) {
        if(obj[propName] != null)
          return
      }
    } else {
      for(String propName in propNames) {
        if(obj.hasProperty(propName) && obj.properties[propName] != null)
          return
      }
    }
    throw new GradleException("Missing at least one of the required properties ${propNames} in ${obj.getClass().getName()}")
  }

  static void requireProperty(Object obj, String propName) {
    if(obj instanceof Map) {
      if(obj[propName] == null)
        throw new GradleException("Missing required property '${propName}' in ${obj.getClass().getName()}")
    } else {
      if(!obj.hasProperty(propName) || obj.properties[propName] == null)
        throw new GradleException("Missing required property '${propName}' in ${obj.getClass().getName()}")
    }
  }

  static resolveClosures(Object obj) {
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

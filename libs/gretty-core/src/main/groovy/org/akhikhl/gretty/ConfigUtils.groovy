/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class ConfigUtils {

  static complementProperties(Object dst, Iterable srcs) {
    complementProperties_(null, dst, srcs)
  }

  static complementProperties(Object dst, Object... srcs) {
    complementProperties_(null, dst, srcs)
  }

  private static complementProperties_(List propNames, Object dst, Object srcs) {
    List dstProps = dst.metaClass.properties.findAll { it.name != 'class' && it.name != 'metaClass' && (propNames == null || propNames.contains(it.name)) }
    for(def src in srcs) {
      if(src instanceof Map)
        for(def prop in dstProps) {
          def propName = prop.name
          if(src[propName] != null && (dst instanceof Map || dst.respondsTo(MetaProperty.getSetterName(propName)))) {
            if(dst[propName] == null)
              dst[propName] = src[propName]
            else if(Collection.class.isAssignableFrom(prop.type))
              dst[propName].addAll(src[propName])
            else if(Map.class.isAssignableFrom(prop.type))
              dst[propName].putAll(src[propName])
          }
        }
      else
        for(def prop in dstProps) {
          def propName = prop.name
          if(src.metaClass.properties.find { it.name == propName } && src[propName] != null &&
             (dst instanceof Map || dst.respondsTo(MetaProperty.getSetterName(propName)))) {
            if(dst[propName] == null)
              dst[propName] = src[propName]
            else if(Collection.class.isAssignableFrom(prop.type))
              dst[propName].addAll(src[propName])
            else if(Map.class.isAssignableFrom(prop.type))
              dst[propName].putAll(src[propName])
          }
        }
    }
    dst
  }

  static complementSpecificProperties(List propNames, Object dst, Iterable srcs) {
    complementProperties_(propNames, dst, srcs)
  }

  static complementSpecificProperties(List propNames, Object dst, Object... srcs) {
    complementProperties_(propNames, dst, srcs)
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
    throw new Exception("Missing at least one of the required properties ${propNames} in ${obj.getClass().getName()}")
  }

  static void requireProperty(Object obj, String propName) {
    if(obj instanceof Map) {
      if(obj[propName] == null)
        throw new Exception("Missing required property '${propName}' in ${obj.getClass().getName()}")
    } else {
      if(!obj.hasProperty(propName) || obj.properties[propName] == null)
        throw new Exception("Missing required property '${propName}' in ${obj.getClass().getName()}")
    }
  }

  static void resolveClosures(Object obj) {
    if(obj == null)
      return
    def props
    if(obj instanceof Map)
      props = obj.keySet()
    else
      props = obj.metaClass.properties.collect { it.name } - ['class']
    for(String propName in props)
      if(obj[propName] instanceof Closure) {
        obj[propName].delegate = obj
        obj[propName].resolveStrategy = Closure.DELEGATE_FIRST
        obj[propName] = obj[propName]()
      }
  }
}

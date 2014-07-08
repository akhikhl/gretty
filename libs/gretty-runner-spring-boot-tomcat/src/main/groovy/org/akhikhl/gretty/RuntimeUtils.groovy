/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file 'license.txt' for copying and usage permission.
 */
package org.akhikhl.gretty

import java.lang.reflect.Constructor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class RuntimeUtils {

  static copy(Map options = [:], obj) {

    if(obj == null)
      return obj

    def t = obj.getClass()

    def cloneMethod = options.cloneMap?.get(t.getName())
    if(cloneMethod)
      return cloneMethod(obj)

    if(t in [Byte, Short, Integer, Long, Float, Double, Character, Boolean, String])
      return obj

    if(Collection.class.isAssignableFrom(t) || Map.class.isAssignableFrom(t))
      return t.newInstance(obj)

    if(t.isEnum())
      return obj

    Constructor[] constructors = t.getDeclaredConstructors()

    Constructor ctor = constructors.find {
      def ptypes = it.getParameterTypes()
      ptypes.length == 1 && ptypes[0].isAssignableFrom(t)
    }
    if(ctor)
      return ctor.newInstance(obj)

    ctor = constructors.find { it.getParameterTypes().length == 0 }
    if(ctor) {
      def result = t.newInstance()
      for(def prop in obj.metaClass.properties.findAll { it.name != 'class' && it.name != 'metaClass' }) {
        def propName = prop.name
        if(options.skipProperties?.contains(propName))
          continue
        if(result.respondsTo(MetaProperty.getSetterName(propName))) {
          def val = options.deepCopy ? copy(options, obj[propName]) : obj[propName]
          result[propName] = val
        }
      }
      return result
    }

    null
  }
}

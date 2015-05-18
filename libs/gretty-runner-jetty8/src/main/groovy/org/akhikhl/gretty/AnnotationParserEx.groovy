/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.eclipse.jetty.annotations.AnnotationParser
import org.eclipse.jetty.annotations.ClassNameResolver
import org.eclipse.jetty.util.MultiException
import org.eclipse.jetty.util.log.Log
import org.eclipse.jetty.util.log.Logger
import org.eclipse.jetty.util.resource.Resource

import java.util.jar.JarEntry
import java.util.jar.JarInputStream

/**
 *
 *  @author akhikhl
 */
class AnnotationParserEx extends AnnotationParser {

  private static final Logger LOG = Log.getLogger(AnnotationParserEx.class)

  private boolean isValidClassFileName (String name) {
    //no name cannot be valid
    if (name == null || name.length()==0)
      return false

    //skip anything that is not a class file
    if (!name.toLowerCase(Locale.ENGLISH).endsWith(".class")) {
      if (LOG.isDebugEnabled()) LOG.debug("Not a class: {}",name);
      return false;
    }

    //skip any classfiles that are not a valid java identifier
    int c0 = 0
    int ldir = name.lastIndexOf('/', name.length()-6)
    c0 = (ldir > -1 ? ldir+1 : c0)
    if (!Character.isJavaIdentifierStart(name.charAt(c0))) {
      if (LOG.isDebugEnabled()) LOG.debug("Not a java identifier: {}"+name)
      return false
    }

    return true
  }

  private boolean isValidClassFilePath (String path) {
    //no path is not valid
    if (path == null || path.length()==0)
      return false

    //skip any classfiles that are in a hidden directory
    if (path.startsWith(".") || path.contains("/.")) {
      if (LOG.isDebugEnabled()) LOG.debug("Contains hidden dirs: {}"+path)
      return false
    }

    return true
  }

  @Override
  public void parse(Resource res, ClassNameResolver resolver) {
    if(res.exists() && !res.isDirectory() && res.toString().endsWith('.jar'))
      parseJar(res, resolver)
    else
      super.parse(res, resolver)
  }

  protected void parseJar(Resource jarResource, ClassNameResolver resolver) {

    InputStream ins = jarResource.getInputStream()
    if (ins == null)
      return

    MultiException me = new MultiException()

    JarInputStream jar_in = new JarInputStream(ins)
    try {
      JarEntry entry = jar_in.getNextJarEntry()
      while(entry != null) {
        try {
          parseJarEntry(jarResource, entry, resolver)
        } catch (Exception e) {
          println "Error scanning entry "+entry.getName()+" from jar "+jarResource + " e=$e"
          me.add(new RuntimeException("Error scanning entry "+entry.getName()+" from jar "+jarResource, e))
        }
        entry = jar_in.getNextJarEntry()
      }
    } finally {
      jar_in.close()
    }
    me.ifExceptionThrow()
  }

  protected void parseJarEntry(Resource jar, JarEntry entry, final ClassNameResolver resolver) {
    if (jar == null || entry == null)
      return

    //skip directories
    if (entry.isDirectory())
      return

    String name = entry.getName()

    //check file is a valid class file name
    if (isValidClassFileName(name) && isValidClassFilePath(name)) {

      String shortName =  name.replace('/', '.').substring(0,name.length()-6)

      if ((resolver == null)
          ||
          (!resolver.isExcluded(shortName) && (!isParsed(shortName) || resolver.shouldOverride(shortName)))) {
        Resource clazz = Resource.newResource("jar:"+jar.getURI()+"!/"+name)
        if (LOG.isDebugEnabled()) {
          LOG.debug("Scanning class from jar {}", clazz)
        }
        scanClass(clazz.getInputStream())
      }
    }
  }
}

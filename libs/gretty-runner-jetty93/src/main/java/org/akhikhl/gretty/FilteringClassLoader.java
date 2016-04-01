/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty;

import org.eclipse.jetty.webapp.WebAppClassLoader;
import sun.misc.CompoundEnumeration;
import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Properly implements "server classes" concept.
 *
 * @author akhikhl
 */
public class FilteringClassLoader extends WebAppClassLoader {

  private final List<String> serverClasses = new ArrayList<String>();

  private final List<String> serverResources = new ArrayList<String>();

  public FilteringClassLoader(Context context) throws IOException {
    super(context);
  }

  public FilteringClassLoader(ClassLoader parent, Context context) throws IOException {
    super(parent, context);
  }

  public void addServerClass(String serverClass) {
    serverClasses.add(serverClass);
    serverResources.add(serverClass.replace('.', '/'));
    serverResources.add("META-INF/services/" + serverClass);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    for(String serverClass : serverClasses)
      if(name.startsWith(serverClass)) {
        Class<?> c = findLoadedClass(name);
        if(c == null)
          c = findClass(name);
        if(c != null) {
          if(resolve)
            resolveClass(c);
          return c;
        }
        throw new ClassNotFoundException(name);
      }
    return super.loadClass(name, resolve);
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    for(String serverResource : serverResources)
      if(name.startsWith(serverResource)) {
        Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
        tmp[0] = getBootstrapResources(name);
        tmp[1] = findResources(name);
        return new CompoundEnumeration(tmp);
      }
    return super.getResources(name);
  }

  private static Enumeration<URL> getBootstrapResources(String name) throws IOException {
    final Enumeration<Resource> e =
            getBootstrapClassPath().getResources(name);
    return new Enumeration<URL> () {
      public URL nextElement() {
        return e.nextElement().getURL();
      }
      public boolean hasMoreElements() {
        return e.hasMoreElements();
      }
    };
  }

  // Returns the URLClassPath that is used for finding system resources.
  static URLClassPath getBootstrapClassPath() {
    return sun.misc.Launcher.getBootstrapClassPath();
  }
}

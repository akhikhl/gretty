/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Properly implements "server classes" concept.
 *
 * @author akhikhl
 */
public class FilteringClassLoader extends URLClassLoader {

  private final List<String> serverClasses = new ArrayList<String>();

  private final List<String> serverResources = new ArrayList<String>();

  private ClassLoader bootClassLoader;

  public FilteringClassLoader(ClassLoader parent) {
    super(new URL[0], parent);
    findBootClassLoader();
  }

  protected void findBootClassLoader() {
    bootClassLoader = getParent();
    if (bootClassLoader != null) {
      while(bootClassLoader.getParent() != null) {
        bootClassLoader = bootClassLoader.getParent();
      }
    }
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
    for(String serverResource : serverResources) {
      if(name.startsWith(serverResource)) {
        final List<URL> resources = new ArrayList<>();
        resources.addAll(Collections.list(getBootstrapResources(name)));
        resources.addAll(Collections.list(findResources(name)));
        return Collections.enumeration(resources);
      }
    }
    return super.getResources(name);
  }

  private Enumeration<URL> getBootstrapResources(String name) throws IOException {
    return bootClassLoader.getResources(name);
  }
}

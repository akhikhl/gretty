/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Implements concept of server classes similar to Jetty server classes.
 *
 * @author akhikhl
 */
public class FilteringClassLoader extends URLClassLoader {

  private final List<String> serverClasses = new ArrayList<String>();

  private final List<String> serverResources = new ArrayList<String>();

  public FilteringClassLoader(ClassLoader parent) {
    super(new URL[0], parent);
  }

  public void addServerClass(String serverClass) {
    serverClasses.add(serverClass);
    serverResources.add(serverClass.replace('.', '/'));
    serverResources.add("META-INF/services/" + serverClass);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    for(String serverClass : serverClasses)
      if(name.startsWith(serverClass))
        throw new ClassNotFoundException(name);
    return super.loadClass(name, resolve);
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    for(String serverResource : serverResources)
      if(name.startsWith(serverResource))
        return Collections.emptyEnumeration();
    return super.getResources(name);
  }
}

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
import org.eclipse.jetty.annotations.*
import org.eclipse.jetty.annotations.AnnotationParser.DiscoverableAnnotationHandler
import org.eclipse.jetty.util.MultiMap
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.webapp.WebAppContext

@CompileStatic(TypeCheckingMode.SKIP)
class AnnotationConfigurationEx extends AnnotationConfiguration {

  private final Set<String> classPath

  AnnotationConfigurationEx(List<String> classPath) {
    this.classPath = classPath as LinkedHashSet
  }

  @Override
  public void configure(WebAppContext context) {

    context.addDecorator(new AnnotationDecorator(context))

    //Even if metadata is complete, we still need to scan for ServletContainerInitializers - if there are any
    if (!context.getMetaData().isMetaDataComplete()) {
      //If metadata isn't complete, if this is a servlet 3 webapp or isConfigDiscovered is true, we need to search for annotations
      if (context.getServletContext().getEffectiveMajorVersion() >= 3 || context.isConfigurationDiscovered()) {
        _discoverableAnnotationHandlers.add(new WebServletAnnotationHandler(context))
        _discoverableAnnotationHandlers.add(new WebFilterAnnotationHandler(context))
        _discoverableAnnotationHandlers.add(new WebListenerAnnotationHandler(context))
      }
    }

    MultiMap map = new MultiMap()
    context.setAttribute(CLASS_INHERITANCE_MAP, map)
    _classInheritanceHandler = new ClassInheritanceHandler(map) {
      public void handle(String className, int version, int access, String signature, String superName, String[] interfaces) {
        super.handle(className, version, access, signature, superName, interfaces)
      }
    }

    //Regardless of metadata, if there are any ServletContainerInitializers with @HandlesTypes, then we need to scan all the
    //classes so we can call their onStartup() methods correctly
    createServletContainerInitializerAnnotationHandlers(context, getNonExcludedInitializers(context))

    if (!_discoverableAnnotationHandlers.isEmpty() || _classInheritanceHandler != null || !_containerInitializerAnnotationHandlers.isEmpty()) {

      parseAnnotations(context)

      for (DiscoverableAnnotationHandler h : _discoverableAnnotationHandlers)
        context.getMetaData().addDiscoveredAnnotations(h.getAnnotationList())
    }
  }

  @Override
  protected AnnotationParser createAnnotationParser() {
    return new AnnotationParserEx()
  }

  private void parseAnnotations(WebAppContext context) {

    def containerIncludeJarPattern = context.getAttribute('org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern')
    if(containerIncludeJarPattern)
      containerIncludeJarPattern = java.util.regex.Pattern.compile(containerIncludeJarPattern)

    def containerExcludeJarPattern = context.getAttribute('org.eclipse.jetty.server.webapp.ContainerExcludeJarPattern')
    if(containerExcludeJarPattern)
      containerExcludeJarPattern = java.util.regex.Pattern.compile(containerExcludeJarPattern)

    AnnotationParser parser = createAnnotationParser()

    for(String classPathElem in classPath) {
      URL url = new URL(classPathElem)
      Resource res = Resource.newResource(url)
      if (res == null)
        continue
      if(containerIncludeJarPattern != null && !(url.toString() =~ containerIncludeJarPattern))
        continue
      if(containerExcludeJarPattern != null && (url.toString() =~ containerExcludeJarPattern))
        continue

      parser.clearHandlers()
      for (DiscoverableAnnotationHandler h : _discoverableAnnotationHandlers)
        if (h instanceof AbstractDiscoverableAnnotationHandler)
          ((AbstractDiscoverableAnnotationHandler)h).setResource(null)

      parser.registerHandlers(_discoverableAnnotationHandlers)
      parser.registerHandler(_classInheritanceHandler)
      parser.registerHandlers(_containerInitializerAnnotationHandlers)

      parser.parse(res, new ClassNameResolver() {

        @Override
        public boolean isExcluded (String name) {
          return context.isSystemClass(name)
        }

        @Override
        public boolean shouldOverride (String name) {
          //looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
          return !context.isParentLoaderPriority()
        }
      })
    }
  }
}


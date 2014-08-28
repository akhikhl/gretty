/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.util.resource.ResourceCollection
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.webapp.WebInfConfiguration

class WebInfConfigurationEx extends WebInfConfiguration implements BaseResourceConfiguration {

  private List extraResourceBases
  private final List baseResourceListeners = []

  WebInfConfigurationEx() {
  }

  @Override
  void addBaseResourceListener(Closure closure) {
    baseResourceListeners.add(closure)
  }

  @Override
  void setExtraResourceBases(List extraResourceBases) {
    this.extraResourceBases = extraResourceBases
  }

  @Override
  public void unpack (WebAppContext context) throws IOException {
    super.unpack(context)
    if(extraResourceBases) {
      Resource res = context.getBaseResource()
      List resources = []
      if(res instanceof ResourceCollection)
        resources.addAll(res.getResources())
      else
        resources.add(res)
      for(def e in extraResourceBases)
        resources.add(Resource.newResource(e))
      context.setBaseResource(new ResourceCollection(resources as Resource[]))
    }
    for(Closure closure in baseResourceListeners)
      closure(context)
  }
}

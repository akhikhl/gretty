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
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.util.resource.ResourceCollection
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.webapp.WebInfConfiguration

@CompileStatic(TypeCheckingMode.SKIP)
class WebInfConfigurationEx extends WebInfConfiguration implements BaseResourceConfiguration {

  private List extraResourceBases
  private final List baseResourceListeners = []

  @Override
  void addBaseResourceListener(Closure closure) {
    baseResourceListeners.add(closure)
  }

  // backported from jetty-9
  @Override
  protected List<Resource> findJars(WebAppContext context) throws Exception {
    List<Resource> jarResources = new ArrayList<Resource>();
    List<Resource> webInfLibJars = findWebInfLibJars(context);
    if (webInfLibJars != null)
      jarResources.addAll(webInfLibJars);
    List<Resource> extraClasspathJars = findExtraClasspathJars(context);
    if (extraClasspathJars != null)
      jarResources.addAll(extraClasspathJars);
    return jarResources;
  }

  // backported from jetty-9
  protected List<Resource> findWebInfLibJars(WebAppContext context) throws Exception {
    Resource web_inf = context.getWebInf();
    if (web_inf==null || !web_inf.exists())
        return null;

    List<Resource> jarResources = new ArrayList<Resource>();
    Resource web_inf_lib = web_inf.addPath("/lib");
    if (web_inf_lib.exists() && web_inf_lib.isDirectory())
    {
        String[] files=web_inf_lib.list();
        for (int f=0;files!=null && f<files.length;f++)
        {
            try
            {
                Resource file = web_inf_lib.addPath(files[f]);
                String fnlc = file.getName().toLowerCase(Locale.ENGLISH);
                int dot = fnlc.lastIndexOf('.');
                String extension = (dot < 0 ? null : fnlc.substring(dot));
                if (extension != null && (extension.equals(".jar") || extension.equals(".zip")))
                {
                    jarResources.add(file);
                }
            }
            catch (Exception ex)
            {
                LOG.warn(Log.EXCEPTION,ex);
            }
        }
    }
    return jarResources;
  }

  // backported from jetty-9
  protected List<Resource> findExtraClasspathJars(WebAppContext context) throws Exception {
    if (context == null || context.getExtraClasspath() == null)
        return null;

    List<Resource> jarResources = new ArrayList<Resource>();
    StringTokenizer tokenizer = new StringTokenizer(context.getExtraClasspath(), ",;");
    while (tokenizer.hasMoreTokens())
    {
        Resource resource = context.newResource(tokenizer.nextToken().trim());
        String fnlc = resource.getName().toLowerCase(Locale.ENGLISH);
        int dot = fnlc.lastIndexOf('.');
        String extension = (dot < 0 ? null : fnlc.substring(dot));
        if (extension != null && (extension.equals(".jar") || extension.equals(".zip")))
        {
            jarResources.add(resource);
        }
    }

    return jarResources;
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

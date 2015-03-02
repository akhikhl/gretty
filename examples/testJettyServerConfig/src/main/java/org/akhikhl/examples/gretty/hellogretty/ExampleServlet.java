/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty.hellogretty;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class ExampleServlet extends HttpServlet {

  private static final long serialVersionUID = -6506276378398106663L;

  private VelocityEngine ve = new VelocityEngine();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Template template = ve.getTemplate("/org/akhikhl/examples/gretty/hellogretty/templates/servletpage.html", "UTF-8");
    VelocityContext context = new VelocityContext();
    context.put("contextPath", request.getContextPath());
    context.put("today", new java.util.Date());
    PrintWriter out = response.getWriter();
    try {
      template.merge(context, out);
      out.flush();
    } finally {
      out.close();
    }
  }

  @Override
  public void init() throws ServletException {
    super.init();
    ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    ve.init();
  }
}


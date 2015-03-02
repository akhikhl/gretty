/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleServlet extends HttpServlet {

  private static final long serialVersionUID = -6506276378398106663L;
  
  private static Logger log = LoggerFactory.getLogger(ExampleServlet.class);

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    log.info("doGet {}", request);
    response.addHeader("Content-Type", "text/plain");
    PrintWriter out = response.getWriter();
    try {
      out.println("Hello, world!");
      out.flush();
    } finally {
      out.close();
    }
  }
}

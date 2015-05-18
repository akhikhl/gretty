package org.akhikhl.gretty;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

public class AppServletInitializer extends SpringBootServletInitializer {

  public static String springBootMainClass;

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    try {
      return application.sources(Class.forName(springBootMainClass, true, AppServletInitializer.class.getClassLoader()));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return application;
    }
  }
}
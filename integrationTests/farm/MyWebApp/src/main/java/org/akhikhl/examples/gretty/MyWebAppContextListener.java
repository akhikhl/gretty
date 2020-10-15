package org.akhikhl.examples.gretty;

import java.io.IOException;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class MyWebAppContextListener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    HttpClient client = new DefaultHttpClient();
    HttpPost request = new HttpPost("http://localhost:8080/MyWebService/getdate");
    HttpResponse response;

    try {
      response = client.execute(request);
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("Web service in farm test is not yet available!");
      }
      EntityUtils.consume(response.getEntity());
    } catch (IOException e) {
      throw new RuntimeException("Web service in farm test is not yet available!");
    } finally {
      client.getConnectionManager().shutdown();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
}
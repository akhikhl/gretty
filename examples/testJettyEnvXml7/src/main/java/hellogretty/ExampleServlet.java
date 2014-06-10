package hellogretty;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class ExampleServlet extends HttpServlet {

  private static final long serialVersionUID = -6506276378398106663L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PropertiesConfiguration config;
    try {
      config = new PropertiesConfiguration();
      config.load(this.getClass().getClassLoader().getResource("hellogretty/messages.properties"));
    } catch (ConfigurationException ex) {
      throw new ServletException(ex);
    }
    String greeting = config.getString("greeting");
    PrintWriter out = response.getWriter();
    try {
      out.println("<html><body><h1>" + greeting + "</h1>");
      out.println("<p>test message 1</p>");
      out.println("</body></html>");
      out.println();
      out.flush();
    } finally {
      out.close();
    }
  }
}

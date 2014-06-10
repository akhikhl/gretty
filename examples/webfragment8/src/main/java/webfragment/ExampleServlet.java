package webfragment;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExampleServlet extends HttpServlet {

  private static final long serialVersionUID = -6506276378398106663L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    try {
      out.println("<html><body><h1>Hello, this is webfragment.ExampleServlet</h1>");
      out.println("<p>generated on " + new Date() + "</p>");
      out.println("</body></html>");
      out.println();
      out.flush();
    } finally {
      out.close();
    }
  }
}

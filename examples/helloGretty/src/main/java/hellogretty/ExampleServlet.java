package hellogretty;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class ExampleServlet extends HttpServlet {

  private static final long serialVersionUID = -6506276378398106663L;

  @Override
  protected void doGet(
      HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {

    try {
      PrintWriter out = response.getWriter();
      try {
        out.println("<html><body><h1>Hello, Gretty! It's fine weather today" + StringUtils.repeat('.', 3) + "</h1></body></html>");
        out.println();
        out.flush();
      } finally {
        out.close();
      }
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
  }
}

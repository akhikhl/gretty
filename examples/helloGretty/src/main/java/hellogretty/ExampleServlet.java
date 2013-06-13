package hellogretty;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.lang3.StringUtils;

public class ExampleServlet extends HttpServlet {

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
    }
    catch (Exception ex) {
      throw new ServletException(ex);
    }
  }
}

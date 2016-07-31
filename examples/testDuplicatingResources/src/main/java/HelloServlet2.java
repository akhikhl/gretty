/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.util.Enumeration;
import java.net.URL;

public class HelloServlet2 extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			Enumeration<URL> res = cl.getResources("META-INF/rxjava.properties");
			List<URL> list = Collections.list(res);

            resp.getOutputStream().print("<p>list size: " + list.size() + "</p>");
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
}

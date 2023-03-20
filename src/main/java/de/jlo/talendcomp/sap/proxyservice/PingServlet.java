package de.jlo.talendcomp.sap.proxyservice;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.jetty.servlet.DefaultServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PingServlet extends DefaultServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(200);
		resp.setContentType("text/plain");
		Writer w = resp.getWriter();
		w.write("pong");
		w.close();
	}


}

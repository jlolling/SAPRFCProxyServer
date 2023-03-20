package de.jlo.talendcomp.sap.proxyservice;

import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ShutdownServlet extends SAPRFCServlet {
	
	private static final long serialVersionUID = 1L;

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		info("Shutdown request received");
		resp.setStatus(200);
		resp.setContentType("test/plain");
		Writer writer = resp.getWriter();
		writer.write("Shutdown server...");
		writer.close();
		try {
			Main.server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

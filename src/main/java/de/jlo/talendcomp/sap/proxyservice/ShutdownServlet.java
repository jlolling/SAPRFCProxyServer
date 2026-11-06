/**
 * Copyright 2023 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jlo.talendcomp.sap.proxyservice;

import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ShutdownServlet extends SAPRFCServlet {
	
	private static final long serialVersionUID = 1L;

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		info("Shutdown request received from: " + req.getRemoteAddr());
		if (isLocalRequest(req) == false) {
			sendError(resp, 403, "Shutdown is only allowed from localhost");
			return;
		}
		resp.setStatus(200);
		resp.setContentType("text/plain");
		Writer writer = resp.getWriter();
		writer.write("Shutdown server...");
		writer.close();
		try {
			Main.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Only direct requests from the loopback interface are allowed.
	 * Requests passed through a proxy (carrying forward headers) are rejected even if the proxy runs on localhost.
	 */
	private boolean isLocalRequest(HttpServletRequest req) {
		if (req.getHeader("X-Forwarded-For") != null || req.getHeader("Forwarded") != null) {
			return false;
		}
		try {
			return InetAddress.getByName(req.getRemoteAddr()).isLoopbackAddress();
		} catch (Exception e) {
			return false;
		}
	}

}

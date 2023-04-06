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
import java.io.Reader;

import org.apache.commons.io.IOUtils;

import de.jlo.talendcomp.sap.Destination;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is a Servlet to run SAP RFC RFC_READ_TABLE requests
 * 
 * @author jan.lolling@gmail.com
 */
public class SAPRFCPingServlet extends SAPRFCServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * Request
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	private void performPing(String payload, HttpServletResponse resp) throws ServletException, IOException {
		try {
			Destination destination = createDestination(payload);
			if (destination != null) {
				destination.ping();
				resp.setStatus(202); // send ok back
				return;
			}
		} catch (Exception e) {
			sendError(resp, 400, "Could not setup destination. Error message: " + e.getMessage());
			return;
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Reader r = req.getReader();
		String payload = IOUtils.toString(r);
		performPing(payload, resp);
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

}

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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.jlo.talendcomp.sap.Destination;
import de.jlo.talendcomp.sap.TableInput;
import de.jlo.talendcomp.sap.TextSplitter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is a Servlet to run SAP RFC RFC_READ_TABLE requests
 * 
 * @author jan.lolling@gmail.com
 */
public class SAPRFCTableInputServlet extends SAPRFCServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * Request
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	private void performQuery(String payload, HttpServletResponse resp) throws ServletException, IOException {
		if (logStatements) {
			info(payload);
		}
		if (payload == null || payload.trim().isEmpty()) {
			sendError(resp, 400, "No payload received");
			return;
		} else {
			resp.setCharacterEncoding("UTF-8");
			ObjectNode root = (ObjectNode) objectMapper.readTree(payload);
			Destination destination;
			try {
				destination = createDestination(payload);
			} catch (ServiceException e1) {
				sendError(resp, e1.getStatusCode(), e1.getMessage());
				return;
			}
			String tableName = getStringValue(root, "tableName");
			if (tableName == null || tableName.trim().isEmpty()) {
				sendError(resp, 400, "Parameter tableName is not set");
				return;
			}
			JsonNode fields = getJsonNode(root, "fields");
			if (fields == null) {
				sendError(resp, 400, "Fields not set");
				return;
			}
			List<String> fieldList = null;
			if (fields.isArray()) {
				ArrayNode fieldArrayNode = (ArrayNode) fields;
				fieldList = new ArrayList<>();
				for (JsonNode fn : fieldArrayNode) {
					fieldList.add(fn.textValue());
				}
			} else {
				fieldList = TextSplitter.split(fields.textValue(), ',');
			}
			TableInput tableInput = destination.createTableInput();
			tableInput.setTableName(tableName);
			for (String f : fieldList) {
				tableInput.addField(f);
			}
			String filter = getStringValue(root, "filter");
			tableInput.setFilter(filter);
			try {
				tableInput.prepare();
			} catch (Exception e) {
				sendError(resp, 500, "Prepare function failed: " + e.getMessage());
				return;
			}
			Integer offset = getIntegerValue(root, "offset");
			tableInput.setRowsToSkip(offset);
			Integer limit = getIntegerValue(root, "limit");
			tableInput.setMaxRows(limit);
			final Writer out = resp.getWriter();
			final Integer keepAliveSeconds = getIntegerValue(root, "keepAliveSeconds");
			Thread keepAliveThread = null;
			if (keepAliveSeconds != null && keepAliveSeconds > 0) {
				keepAliveThread = new Thread() {
					
					private boolean statusSet = false;
					
					@Override
					public void run() {
						while (true) {
							if (isInterrupted() == false) {
								try {
									sleep(keepAliveSeconds * 1000);
									if (statusSet == false) {
										resp.setStatus(200);
										statusSet = true;
									}
									out.write("executing\n");
									out.flush();
								} catch (Exception e) {
									break;
								}
							}
						}
					}
					
				};		
				keepAliveThread.start();
			}
			try {
				tableInput.execute();
				if (keepAliveThread != null) {
					if (keepAliveThread.isAlive()) {
						keepAliveThread.interrupt();
					}
					keepAliveThread = null;
				}
			} catch (Exception e) {
				sendError(resp, 500, "Execute function failed: " + e.getMessage());
				return;
			}
			if (logStatements) {
				info(tableInput.getFunctionDescription());
			}
			resp.setHeader("total-rows", String.valueOf(tableInput.getTotalRowCount()));
			resp.setContentType("application/json");
			try (BufferedWriter br = new BufferedWriter(out)) {
				br.write("[\n");
				boolean firstLoop = true;
				while (tableInput.next()) {
					List<String> oneRow = tableInput.getCurrentRow();
					System.out.println(oneRow.get(0));
					ArrayNode oneRowArrayNode = objectMapper.createArrayNode();
					for (String v : oneRow) {
						oneRowArrayNode.add(v);
					}
					if (firstLoop) {
						firstLoop = false;
					} else {
						br.write(",\n");
						br.flush();
					}
					String json = objectMapper.writeValueAsString(oneRowArrayNode);
					br.write(json);
				}
				br.write("\n]\n");
				br.flush();
			} catch (Exception e) {
				sendError(resp, 500, "Processing SAP RFC response failed: " + e.getMessage());
			}
			tableInput = null;
		}		
	}
	
	private List<String> createTestRow(int currentRowIndex, int countColumns) {
		List<String> row = new ArrayList<>();
		for (int i = 0; i < countColumns; i++) {
			String v = "V" + currentRowIndex + "-" + i;
			row.add(v);
		}
		return row;
	}
	
	private void performTestoutput(int numTestRecords, int numTestColumns, HttpServletResponse resp) throws ServletException, IOException {
		final Writer out = resp.getWriter();
		try (BufferedWriter br = new BufferedWriter(out)) {
			resp.setContentType("application/json");
			resp.setStatus(200);
			resp.setHeader("total-rows", String.valueOf(numTestRecords));
			br.write("[\n");
			boolean firstLoop = true;
			for (int i = 0; i < numTestRecords; i++) {
				ArrayNode outrow = objectMapper.createArrayNode();
				List<String> inrow = createTestRow(i, numTestColumns);
				for (String v : inrow) {
					outrow.add(v);
				}
				if (firstLoop) {
					firstLoop = false;
				} else {
					br.write(",\n");
				}
				br.write(objectMapper.writeValueAsString(outrow));
				br.flush();
			}
			br.write("\n]\n");
			br.flush();
		} catch (Exception e) {
			sendError(resp, 500, "Processing SAP RFC response failed: " + e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String testrowsStr = req.getParameter("testrows");
		if (testrowsStr == null || testrowsStr.trim().isEmpty()) {
			Reader r = req.getReader();
			String payload = IOUtils.toString(r);
			performQuery(payload, resp);
		} else {
			resp.setCharacterEncoding("UTF-8");
			int testRows = 1000;
			try {
				testRows = Integer.parseInt(testrowsStr);
			} catch (NumberFormatException nfe) {
				warn("Parameter value for testrows is not a number: " + testrowsStr + ". Service use default value: " + testRows);
			}
			String testcolStr = req.getParameter("testcols");
			int testCols = 10;
			try {
				testCols = Integer.parseInt(testcolStr);
			} catch (NumberFormatException nfe) {
				warn("Parameter value for testcols is not a number: " + testcolStr + ". Service use default value: " + testCols);
			}
			performTestoutput(testRows, testCols, resp);
		}
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

}

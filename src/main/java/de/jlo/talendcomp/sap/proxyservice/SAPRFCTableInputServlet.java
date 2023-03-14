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
			ObjectNode root = (ObjectNode) objectMapper.readTree(payload);
			Destination destination;
			try {
				destination = createDestination(payload);
			} catch (ServiceException e1) {
				sendError(resp, e1.getStatusCode(), e1.getMessage());
				return;
			}
			String tableName = root.get("tableName").asText();
			if (tableName == null || tableName.trim().isEmpty()) {
				sendError(resp, 400, "Parameter tableName is not set");
				return;
			}
			JsonNode fields = root.get("fields");
			if (fields == null || fields.isMissingNode()) {
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
			JsonNode filterNode = root.get("filter");
			String filter = null;
			if (filterNode != null) {
				filter = filterNode.asText();
			}
			int offset = 0;
			JsonNode offsetNode = root.get("offset");
			if (offsetNode != null) {
				offset = offsetNode.asInt(0);
			}
			int limit = 0;
			JsonNode limitNode = root.get("limit");
			if (limitNode != null ) {
				limit = limitNode.asInt(0);
			}
			TableInput tableInput = destination.createTableInput();
			tableInput.setTableName(tableName);
			for (String f : fieldList) {
				tableInput.addField(f);
			}
			tableInput.setFilter(filter);
			try {
				tableInput.prepare();
			} catch (Exception e) {
				sendError(resp, 500, "Prepare function failed: " + e.getMessage());
				return;
			}
			tableInput.setRowsToSkip(offset);
			tableInput.setMaxRows(limit);
			try {
				tableInput.execute();
			} catch (Exception e) {
				sendError(resp, 500, "Execute function failed: " + e.getMessage());
				return;
			}
			if (logStatements) {
				info(tableInput.getFunctionDescription());
			}
			resp.setHeader("total-rows", String.valueOf(tableInput.getTotalRowCount()));
			final Writer out = resp.getWriter();
			try (BufferedWriter br = new BufferedWriter(out)) {
				resp.setContentType("application/json");
				resp.setStatus(200);
				br.write("[\n");
				boolean firstLoop = true;
				while (tableInput.next()) {
					List<String> oneRow = tableInput.getCurrentRow();
					ArrayNode oneRowArrayNode = objectMapper.createArrayNode();
					for (String v : oneRow) {
						oneRowArrayNode.add(v);
					}
					if (firstLoop) {
						firstLoop = false;
					} else {
						br.write(",\n");
					}
					br.write(objectMapper.writeValueAsString(oneRowArrayNode));
					br.flush();
				}
				br.write("\n]");
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
			br.write("\n]");
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

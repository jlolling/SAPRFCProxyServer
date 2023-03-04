package de.jlo.talendcomp.sap.proxyservice;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.servlet.DefaultServlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.jlo.talendcomp.sap.ApplicationServerProperties;
import de.jlo.talendcomp.sap.ConnectionProperties;
import de.jlo.talendcomp.sap.Destination;
import de.jlo.talendcomp.sap.Driver;
import de.jlo.talendcomp.sap.DriverManager;
import de.jlo.talendcomp.sap.MessageServerProperties;
import de.jlo.talendcomp.sap.TableInput;
import de.jlo.talendcomp.sap.TalendContextPasswordUtil;
import de.jlo.talendcomp.sap.TextSplitter;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is a Servlet to run SAP RFC RFC_READ_TABLE requests
 * 
 * @author jan.lolling@gmail.com
 */
public class SAPRFCTableInputServlet extends DefaultServlet {

	private static final long serialVersionUID = 1L;
	private Driver driver = null;
	private final static ObjectMapper objectMapper = new ObjectMapper();
	private boolean logStatements = false;
	private Map<String, Properties> mapDestinationProperties = new HashMap<>();
	private String propertyFileDir = null;

	public boolean isLogStatements() {
		return logStatements;
	}

	public void setLogStatements(boolean logStatements) {
		this.logStatements = logStatements;
	}

	/**
	 * Request
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	private String getStringValue(ObjectNode node, String attribute) {
		if (node == null) {
			throw new IllegalArgumentException("Node name cannot be null");
		}
		if (attribute == null || attribute.trim().isEmpty()) {
			throw new IllegalArgumentException("attribute name cannot be null or empty");
		}
		JsonNode n = node.get(attribute);
		if (n != null) {
			return n.textValue();
		} else {
			return null;
		}
	}
	
	private void performQuery(String payload, HttpServletResponse resp) throws ServletException, IOException {
		if (logStatements) {
			System.out.println(payload);
		}
		if (payload == null || payload.trim().isEmpty()) {
			System.err.println("No payload received");
			resp.sendError(400, "No payload received");
			return;
		} else {
			ObjectNode root = (ObjectNode) objectMapper.readTree(payload);
			ObjectNode destNode = (ObjectNode) root.get("destination");
			String destinationName = getStringValue(destNode, "destinationName");
			String type = null;
			String password = null;
			String host = null;
			String client = null;
			String user = null;
			String language = null;
			String group = null;
			String r3Name = null;
			String systemNumber = null;
			if (destinationName != null && destinationName.trim().isEmpty() == false) {
				// load parameters for destination from the loaded properties
				// get the properties
				Properties destinationProps = mapDestinationProperties.get(destinationName);
				if (destinationProps == null || destinationProps.size() == 0) {
					System.err.println("No payload received");
					resp.sendError(400, "No destinationProperties for the name: " + destinationName + " available");
					return;
				}
				type = destinationProps.getProperty("destinationType");
				password = destinationProps.getProperty("password");
				if (password == null || password.trim().isEmpty()) {
					resp.sendError(400, "Password not set");
					return;
				}
				password = TalendContextPasswordUtil.decryptPassword(password);
				host = destinationProps.getProperty("host");
				client = destinationProps.getProperty("client");
				user = destinationProps.getProperty("user");
				language = destinationProps.getProperty("language");
				group = destinationProps.getProperty("group");
				r3Name = destinationProps.getProperty("r3name");
				systemNumber = destinationProps.getProperty("systemNumber");
			} else {
				// take parameters for destination from the payload
				type = getStringValue(destNode, "destinationType");
				password = getStringValue(destNode, "password");
				if (password == null || password.trim().isEmpty()) {
					resp.sendError(400, "Password not set");
					return;
				}
				password = TalendContextPasswordUtil.decryptPassword(password);
				host = getStringValue(destNode, "host");
				client = getStringValue(destNode, "client");
				user = getStringValue(destNode, "user");
				language = getStringValue(destNode, "language");
				group = getStringValue(destNode, "group");
				r3Name = getStringValue(destNode, "r3name");
				systemNumber = getStringValue(destNode, "systemNumber");
			}
			ConnectionProperties connProps = null;
			if ("message_server".equals(type)) {
				connProps = new MessageServerProperties()
						.setHost(host)
						.setClient(client)
						.setUser(user)
						.setPassword(password)
						.setLanguage(language)
						.setGroup(group)
						.setR3Name(r3Name);
			} else if ("application_server".equals(type)) {
				connProps = new ApplicationServerProperties()
						.setHost(host)
						.setClient(client)
						.setUser(user)
						.setPassword(password)
						.setLanguage(language)
						.setSystemNumber(systemNumber);
			} else {
				System.err.println("Invalid destinationType: " + type);
				resp.sendError(400, "Invalid destinationType: " + type);
				return;
			}
			Destination destination = null;
			try {
				destination = driver.getDestination(connProps);
			} catch (Exception e) {
				System.err.println("Could not setup destination. Error message: " + e.getMessage());
				resp.sendError(400, "Could not setup destination. Error message: " + e.getMessage());
				return;
			}
			String tableName = root.get("tableName").asText();
			if (tableName == null || tableName.trim().isEmpty()) {
				System.err.println("Parameter tableName is not set");
				resp.sendError(400, "Parameter tableName is not set");
				return;
			}
			JsonNode fields = root.get("fields");
			if (fields == null || fields.isMissingNode()) {
				System.err.println("Fields not set");
				resp.sendError(400, "Fields not set");
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
			String filter = root.get("filter").textValue();
			int offset = root.get("offset").intValue();
			int limit = root.get("limit").intValue();
			TableInput tableInput = destination.createTableInput();
			tableInput.setTableName(tableName);
			for (String f : fieldList) {
				tableInput.addField(f);
			}
			tableInput.setFilter(filter);
			try {
				tableInput.prepare();
			} catch (Exception e) {
				System.err.println("Prepare function failed: " + e.getMessage());
				resp.sendError(500, "Prepare function failed: " + e.getMessage());
				return;
			}
			tableInput.setRowsToSkip(offset);
			tableInput.setMaxRows(limit);
			try {
				tableInput.execute();
			} catch (Exception e) {
				System.err.println("Execute function failed: " + e.getMessage());
				resp.sendError(500, "Execute function failed: " + e.getMessage());
				return;
			}
			if (logStatements) {
				System.out.println(tableInput.getFunctionDescription());
			}
			final Writer out = resp.getWriter();
			try (BufferedWriter br = new BufferedWriter(out)) {
				resp.setContentType("application/json");
				resp.setStatus(200);
				br.write("[\n");
				boolean firstLoop = true;
				while (tableInput.next()) {
					ArrayNode outrow = objectMapper.createArrayNode();
					List<String> inrow = tableInput.getCurrentRow();
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
				System.err.println("Processing SAP RFC response failed: " + e.getMessage());
				resp.sendError(500, "Processing SAP RFC response failed: " + e.getMessage());
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
			System.err.println("Processing SAP RFC response failed: " + e.getMessage());
			resp.sendError(500, "Processing SAP RFC response failed: " + e.getMessage());
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
				System.err.println("Parameter value for testrows is not a number: " + testrowsStr + ". Service use default value: " + testRows);
			}
			String testcolStr = req.getParameter("testcols");
			int testCols = 10;
			try {
				testCols = Integer.parseInt(testcolStr);
			} catch (NumberFormatException nfe) {
				System.err.println("Parameter value for testcols is not a number: " + testcolStr + ". Service use default value: " + testCols);
			}
			performTestoutput(testRows, testCols, resp);
		}
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	public void setup() throws UnavailableException {
		if (driver == null) {
			try {
				driver = DriverManager.getDriver();
			} catch (Exception e) {
				throw new UnavailableException("Load driver failed: " + e.getMessage());
			}
		}
	}

	public String getPropertyFileDir() {
		return propertyFileDir;
	}

	public void setPropertyFileDir(String propertyFileDir) {
		this.propertyFileDir = propertyFileDir;
	}

}

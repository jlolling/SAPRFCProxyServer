package de.jlo.talendcomp.sap.proxyservice;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.List;

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

	/**
	 * Request
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Reader r = req.getReader();
		String payload = IOUtils.toString(r);
		if (payload == null || payload.trim().isEmpty()) {
			resp.sendError(400, "No payload received");
			return;
		} else {
			ObjectNode root = (ObjectNode) objectMapper.readTree(payload);
			ObjectNode destNode = (ObjectNode) root.get("destination");
			String type = destNode.get("destinationType").asText();
			String password = destNode.get("password").textValue();
			if (password == null || password.trim().isEmpty()) {
				resp.sendError(400, "Password not set");
				return;
			}
			password = TalendContextPasswordUtil.decryptPassword(password);
			ConnectionProperties connProps = null;
			if ("message_server".equals(type)) {
				connProps = new MessageServerProperties()
						.setHost(destNode.get("host").textValue())
						.setClient(destNode.get("client").textValue())
						.setUser(destNode.get("user").textValue())
						.setPassword(password)
						.setLanguage(destNode.get("language").textValue())
						.setGroup(destNode.get("group").textValue())
						.setR3Name(destNode.get("r3name").textValue());
			} else if ("application_server".equals(type)) {
				connProps = new ApplicationServerProperties()
						.setHost(destNode.get("host").textValue())
						.setClient(destNode.get("client").textValue())
						.setUser(destNode.get("user").textValue())
						.setPassword(password)
						.setLanguage(destNode.get("language").textValue())
						.setSystemNumber(destNode.get("systemNumber").textValue());
			} else {
				resp.sendError(400, "Invalid destinationType: " + type);
				return;
			}
			Destination destination = null;
			try {
				destination = driver.getDestination(connProps);
			} catch (Exception e) {
				resp.sendError(400, "Could not setup destination. Error message: " + e.getMessage());
				return;
			}
			String tableName = root.get("tableName").asText();
			if (tableName == null || tableName.trim().isEmpty()) {
				resp.sendError(400, "Parameter tableName is not set");
				return;
			}
			JsonNode fields = root.get("fields");
			if (fields.isMissingNode()) {
				resp.sendError(400, "Fields not set");
				return;
			}
			if (fields.isArray() == false) {
				resp.sendError(400, "Fields must provided as array");
				return;
			}
			ArrayNode fieldArrayNode = (ArrayNode) fields;
			String filter = root.get("filter").textValue();
			Integer offset = root.get("offset").intValue();
			Integer limit = root.get("limit").intValue();
			TableInput tableInput = destination.createTableInput();
			tableInput.setTableName(tableName);
			for (JsonNode fn : fieldArrayNode) {
				tableInput.addField(fn.textValue());
			}
			tableInput.setFilter(filter);
			tableInput.setRowsToSkip(offset);
			tableInput.setMaxRows(limit);
			try {
				tableInput.execute();
			} catch (Exception e) {
				resp.sendError(500, "Execute query failed: " + e.getMessage());
				return;
			}
			final OutputStream out = resp.getOutputStream();
			try (BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
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
				resp.sendError(500, "Processing SAP RFC response failed: " + e.getMessage());
			}
			tableInput = null;
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

}

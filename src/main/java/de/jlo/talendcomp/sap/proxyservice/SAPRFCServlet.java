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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.jlo.talendcomp.sap.ApplicationServerProperties;
import de.jlo.talendcomp.sap.ConnectionProperties;
import de.jlo.talendcomp.sap.Destination;
import de.jlo.talendcomp.sap.Driver;
import de.jlo.talendcomp.sap.DriverManager;
import de.jlo.talendcomp.sap.MessageServerProperties;
import de.jlo.talendcomp.sap.TalendContextPasswordUtil;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletResponse;

public abstract class SAPRFCServlet extends DefaultServlet {

	private static Logger log = LogManager.getLogger(SAPRFCServlet.class);
	private static final long serialVersionUID = 1L;
	private Driver driver = null;
	protected final static ObjectMapper objectMapper = new ObjectMapper();
	protected boolean logStatements = false;
	private Map<String, Properties> mapDestinationProperties = new HashMap<>();
	private String propertyFileDir = null;

	public void setup() throws UnavailableException {
		if (driver == null) {
			try {
				driver = DriverManager.getDriverSAPJCO();
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

	public Driver getDriver() {
		return driver;
	}
	
	public boolean isLogStatements() {
		return logStatements;
	}

	public void setLogStatements(boolean logStatements) {
		this.logStatements = logStatements;
	}

	protected JsonNode getJsonNode(JsonNode node, String attribute) {
		if (node == null) {
			throw new IllegalArgumentException("Node name cannot be null");
		}
		if (attribute == null || attribute.trim().isEmpty()) {
			throw new IllegalArgumentException("attribute name cannot be null or empty");
		}
		JsonNode n = node.get(attribute);
		if (n != null && n.isNull() == false && n.isMissingNode() == false) {
			return n;
		} else {
			return null;
		}
	}

	protected String getStringValue(JsonNode node, String attribute) {
		if (node == null) {
			throw new IllegalArgumentException("Node name cannot be null");
		}
		if (attribute == null || attribute.trim().isEmpty()) {
			throw new IllegalArgumentException("attribute name cannot be null or empty");
		}
		JsonNode n = getJsonNode(node, attribute);
		if (n != null) {
			return n.asText();
		} else {
			return null;
		}
	}

	protected Integer getIntegerValue(JsonNode node, String attribute) {
		if (node == null) {
			throw new IllegalArgumentException("Node name cannot be null");
		}
		if (attribute == null || attribute.trim().isEmpty()) {
			throw new IllegalArgumentException("attribute name cannot be null or empty");
		}
		JsonNode n = getJsonNode(node, attribute);
		if (n != null) {
			return n.intValue();
		} else {
			return null;
		}
	}

	protected Double getDoubleValue(JsonNode node, String attribute) {
		if (node == null) {
			throw new IllegalArgumentException("Node name cannot be null");
		}
		if (attribute == null || attribute.trim().isEmpty()) {
			throw new IllegalArgumentException("attribute name cannot be null or empty");
		}
		JsonNode n = getJsonNode(node, attribute);
		if (n != null) {
			return n.doubleValue();
		} else {
			return null;
		}
	}

	protected Boolean getBooleanValue(JsonNode node, String attribute) {
		if (node == null) {
			throw new IllegalArgumentException("Node name cannot be null");
		}
		if (attribute == null || attribute.trim().isEmpty()) {
			throw new IllegalArgumentException("attribute name cannot be null or empty");
		}
		JsonNode n = getJsonNode(node, attribute);
		if (n != null) {
			return n.asBoolean();
		} else {
			return null;
		}
	}

	protected Destination createDestination(String payload) throws ServiceException {
		if (logStatements) {
			info(payload);
		}
		if (payload == null || payload.trim().isEmpty()) {
			throw new ServiceException(400, "No payload received");
		} else {
			ObjectNode root;
			try {
				root = (ObjectNode) objectMapper.readTree(payload);
			} catch (JsonProcessingException e1) {
				throw new ServiceException(400, "Invalid payload: " + e1.getMessage(), e1);
			}
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
					throw new ServiceException(400, "No destinationProperties for the name: " + destinationName + " available");
				}
				type = destinationProps.getProperty("destinationType");
				password = destinationProps.getProperty("password");
				if (password == null || password.trim().isEmpty()) {
					throw new ServiceException(400, "Password not set");
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
					throw new ServiceException(400, "Password not set");
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
				warn("Invalid destinationType: " + type);
				throw new ServiceException(400, "Invalid destinationType: " + type);
			}
			Destination destination = null;
			try {
				// ping will performed in getDestination
				destination = driver.getDestination(connProps);
				return destination;
			} catch (Exception e) {
				throw new ServiceException(400, "Could not setup destination. Error message: " + e.getMessage(), e);
			}
		}		
	}
	
	public void sendError(HttpServletResponse resp, int code, String message) {
		log.error(this.getClass().getSimpleName() + ": code: " + code + " message: " + message);
		try {
			resp.sendError(code, message);
		} catch (IOException e) {
			log.error("sendError code: " + code + " message: " + message + " failed: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void info(String message) {
		log.info(this.getClass().getSimpleName() + ": " + message);
	}

	public void warn(String message) {
		log.warn(this.getClass().getSimpleName() + ": " + message);
	}

}

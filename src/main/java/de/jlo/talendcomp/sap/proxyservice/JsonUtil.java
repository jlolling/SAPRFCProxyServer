package de.jlo.talendcomp.sap.proxyservice;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtil {
	
	public static JsonNode getJsonNode(JsonNode node, String attribute) {
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

	public static String getStringValue(JsonNode node, String attribute) {
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

	public static Integer getIntegerValue(JsonNode node, String attribute) {
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

	public static Double getDoubleValue(JsonNode node, String attribute) {
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

	public static Boolean getBooleanValue(JsonNode node, String attribute) {
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


}

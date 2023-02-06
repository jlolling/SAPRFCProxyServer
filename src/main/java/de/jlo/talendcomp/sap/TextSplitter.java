package de.jlo.talendcomp.sap;

import java.util.ArrayList;
import java.util.List;

public class TextSplitter {
	
	public static List<String> split(String text, char delimiter) {
		List<String> result = new ArrayList<>();
		if (text != null) {
			text = text.trim();
			boolean inLiteral = false;
			int i = 0;
			StringBuilder sb = new StringBuilder();
			for (; i < text.length(); i++) {
				char c = text.charAt(i);
				if (c == '\'') {
					if (inLiteral == false) {
						inLiteral = true;
					} else {
						inLiteral = false;
					}
				}
				if (inLiteral) {
					sb.append(c);
				} else {
					if (c == delimiter) {
						// found delimiter to cut
						// but ignore 2 following delimiter
						String part = sb.toString();
						if (part.trim().isEmpty() == false) {
							result.add(part);
						}
						sb.setLength(0); // reset string buffer
					} else {
						sb.append(c);
					}
				}
			}
			String lastPart = sb.toString();
			if (lastPart.trim().isEmpty() == false) {
				result.add(lastPart);
			}
		}
		return result;
	}
	
	
}

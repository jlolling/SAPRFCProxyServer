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

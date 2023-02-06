package de.jlo.talendcomp.sap.impl;

import com.sap.conn.jco.JCoDestination;

import de.jlo.talendcomp.sap.Destination;
import de.jlo.talendcomp.sap.TableInput;

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
public class DestinationImpl implements Destination {
	
	private JCoDestination jcoDestination = null;
	
	public DestinationImpl(JCoDestination jcoDestination) {
		if (jcoDestination == null) {
			throw new IllegalArgumentException("jcoDestination cannot be null");
		}
		this.jcoDestination = jcoDestination;
	}
	
	@Override
	public TableInput createTableInput() {
		TableInputImpl ti = new TableInputImpl(jcoDestination);
		return ti;
	}
	
}
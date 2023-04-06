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
package de.jlo.talendcomp.sap.sapjco;

import de.jlo.talendcomp.sap.ConnectionProperties;
import de.jlo.talendcomp.sap.Destination;
import de.jlo.talendcomp.sap.Driver;

public class DriverImpl implements Driver {
	
	public DriverImpl() {}
	
	private static boolean providerRegistered = false;

	@Override
	public Destination getDestination(ConnectionProperties connProp) throws Exception {
		DestinationImpl d = new DestinationImpl(getJCoDestination(connProp));
		return d;
	}

	/**
	 * Creates the action connection (SAP name it destination)
	 * @return destination (means actually a kind of connection to SAP server)
	 * @throws Exception
	 */
	private com.sap.conn.jco.JCoDestination getJCoDestination(ConnectionProperties connProp) throws Exception {
		if (connProp == null) {
			throw new IllegalStateException("Connection properties are null and not created before");
		}
		connProp.build();
		if (providerRegistered == false) {
			com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(ProxyDestinationDataProvider.getInstance());
			providerRegistered = true;
		}
		ProxyDestinationDataProvider.getInstance().setConnectionProperties(connProp);
		// JCoDestinationManager will use the register MyDestinationDataProvider
		com.sap.conn.jco.JCoDestination dest = com.sap.conn.jco.JCoDestinationManager.getDestination(connProp.getDestinationName());
		return dest;
	}
	
}

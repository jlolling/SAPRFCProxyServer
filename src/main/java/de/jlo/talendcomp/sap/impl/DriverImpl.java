package de.jlo.talendcomp.sap.impl;

import de.jlo.talendcomp.sap.ConnectionProperties;
import de.jlo.talendcomp.sap.Destination;
import de.jlo.talendcomp.sap.Driver;

public class DriverImpl implements Driver {
	
	public DriverImpl() {}
	
	private static boolean providerRegistered = false;

	@Override
	public Destination createDestination(ConnectionProperties connProp) throws Exception {
		DestinationImpl d = new DestinationImpl(createJCoDestination(connProp));
		return d;
	}

	/**
	 * Creates the action connection (SAP name it destination)
	 * @return destination (means actually a kind of connection to SAP server)
	 * @throws Exception
	 */
	private com.sap.conn.jco.JCoDestination createJCoDestination(ConnectionProperties connProp) throws Exception {
		if (connProp == null) {
			throw new IllegalStateException("Connection properties are not created before");
		}
		connProp.build();
		if (providerRegistered == false) {
			com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(ProxyDestinationDataProvider.getInstance());
			providerRegistered = true;
		}
		ProxyDestinationDataProvider.getInstance().setConnectionProperties(connProp);
		// JCoDestinationManager will use the register MyDestinationDataProvider
		com.sap.conn.jco.JCoDestination dest = com.sap.conn.jco.JCoDestinationManager.getDestination(connProp.getDestinationName());
		dest.ping();
		return dest;
	}
	
}

package de.jlo.talendcomp.sap;

public interface Driver {
	
	public Destination createDestination(ConnectionProperties connProp) throws Exception;

}

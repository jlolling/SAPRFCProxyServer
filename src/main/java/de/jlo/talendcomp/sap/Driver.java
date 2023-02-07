package de.jlo.talendcomp.sap;

public interface Driver {
	
	public Destination getDestination(ConnectionProperties connProp) throws Exception;

}

package de.jlo.talendcomp.sap;

public interface Destination {
	
	public TableInput createTableInput();
	
	public void ping() throws Exception;
	
	public void close();
	
}

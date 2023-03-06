package de.jlo.talendcomp.sap.proxyservice;

public class ServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int statusCode = 0;
	
	public int getStatusCode() {
		return statusCode;
	}

	public ServiceException(int code, String message) {
		super(message);
		this.statusCode = code;
	}

	public ServiceException(int code, String message, Throwable t) {
		super(message, t);
		this.statusCode = code;
	}
	
}

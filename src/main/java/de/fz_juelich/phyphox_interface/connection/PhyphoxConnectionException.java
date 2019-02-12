package de.fz_juelich.phyphox_interface.connection;

public class PhyphoxConnectionException extends Exception {
	
	private static final long serialVersionUID = -2770617053742967082L;
	
	public PhyphoxConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
	public PhyphoxConnectionException(String message) {
		super(message);
	}
	public PhyphoxConnectionException(Throwable cause) {
		super(cause);
	}
}
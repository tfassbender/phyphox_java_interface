package de.fz_juelich.phyphox_interface.connection;

/**
 * Simple class to hold the IP and port for a connection to the phone in remote mode. 
 */
public class PhyphoxConnection {
	
	private String ip;
	private int port;
	
	public PhyphoxConnection(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public String getUrlAsString() {
		return ip + ":" + port;
	}
}
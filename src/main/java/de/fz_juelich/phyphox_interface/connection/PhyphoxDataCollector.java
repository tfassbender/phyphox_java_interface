package de.fz_juelich.phyphox_interface.connection;

/**
 * The connection to phyphox (via REST) is implemented here. The requests are sent to the phone (in remote mode) and the raw data is received in this
 * class. The data is not kept here but is just passed on (Transformed and usable data is to be found in the class PhyphoxData in the data package).
 */
public class PhyphoxDataCollector {
	
	private PhyphoxConnection connection;
	
	public PhyphoxDataCollector(PhyphoxConnection connection) {
		this.connection = connection;
	}
	
	public PhyphoxRawData getData(String request) {
		//TODO
		return null;
	}
	
	public PhyphoxRawData getData(PhyphoxDataRequest request) {
		//TODO
		return null;
	}
}
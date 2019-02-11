package de.fz_juelich.phyphox_interface.connection;

import java.util.List;

/**
 * A wrapper class to create request, used to get the data from the phone (in remote mode).<br>
 * The requests are sent to the phone via the PhyphoxDataRequest class.
 */
public class PhyphoxDataRequest {
	
	private List<String> buffers;
	private List<String> offsets;
	
	public PhyphoxDataRequest(List<String> buffers, List<String> offsets) {
		this.buffers = buffers;
		this.offsets = offsets;
	}
	
	public String getAsString() {
		//TODO
		return null;
	}
}
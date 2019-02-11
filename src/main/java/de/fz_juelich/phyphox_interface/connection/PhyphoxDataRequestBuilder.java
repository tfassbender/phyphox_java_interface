package de.fz_juelich.phyphox_interface.connection;

import java.util.List;

/**
 * A builder class (following the builder pattern) that is used to create a PhyphoxREquest object.
 */
public class PhyphoxDataRequestBuilder {
	
	private List<String> buffers;
	private List<String> offsets;
	
	public PhyphoxDataRequest build() {
		//TODO
		return null;
	}
	
	public void setBuffer(int index, String name) {
		//TODO
	}
	
	public void setOffset(int index, String offset) {
		//TODO
	}
	
	public void setOffsetToBuffer(int index, int bufferIndex) {
		//TODO
	}
	
	public void reset() {
		//TODO
	}
}
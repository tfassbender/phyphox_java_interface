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
		if (buffers.size() != offsets.size()) {
			throw new IllegalArgumentException("The number of buffers and the number of offsets have to be equal.");
		}
		this.buffers = buffers;
		this.offsets = offsets;
		replaceNullWithFull(offsets);
	}
	
	public String getAsString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < buffers.size(); i++) {
			sb.append(buffers.get(i));
			sb.append("=");
			sb.append(offsets.get(i));
			sb.append("&");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PhyphoxDataRequest) {
			PhyphoxDataRequest request = (PhyphoxDataRequest) obj;
			return request.getAsString().equals(this.getAsString());
		}
		else {
			return super.equals(obj);
		}
	}
	
	/**
	 * Replace all null values with the text "full" for full updates.
	 */
	private void replaceNullWithFull(List<String> offsets) {
		for (int i = 0; i < offsets.size(); i++) {
			if (offsets.get(i) == null) {
				offsets.set(i, "full");
			}
		}
	}
}
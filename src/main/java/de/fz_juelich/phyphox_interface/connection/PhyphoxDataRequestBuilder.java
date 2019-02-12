package de.fz_juelich.phyphox_interface.connection;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder class (following the builder pattern) that is used to create a PhyphoxREquest object.
 */
public class PhyphoxDataRequestBuilder {
	
	private List<String> buffers;
	private List<String> offsets;
	
	public PhyphoxDataRequestBuilder() {
		reset();
	}

	public PhyphoxDataRequest build() {
		return new PhyphoxDataRequest(buffers, offsets);
	}
	
	public PhyphoxDataRequestBuilder setBuffer(int index, String name) {
		expandListSize(buffers, index);
		buffers.set(index, name);
		return this;
	}
	
	public PhyphoxDataRequestBuilder setOffset(int index, String offset) {
		expandListSize(offsets, index);
		offsets.set(index, offset);
		return this;
	}
	public PhyphoxDataRequestBuilder setOffset(int index, double offset) {
		return setOffset(index, Double.toString(offset));
	}
	
	public PhyphoxDataRequestBuilder setOffsetToBuffer(int index, int bufferIndex, String bufferOffset) {
		expandListSize(offsets, index);
		String offset = bufferOffset + "|" + buffers.get(bufferIndex);
		offsets.set(index, offset);
		return this;
	}
	public PhyphoxDataRequestBuilder setOffsetToBuffer(int index, int bufferIndex, double bufferOffset) {
		return setOffsetToBuffer(index, bufferIndex, Double.toString(bufferOffset));
	}
	
	public void reset() {
		buffers = new ArrayList<String>();
		offsets = new ArrayList<String>();
	}
	
	private void expandListSize(List<String> list, int indexNeeded) {
		if (indexNeeded >= list.size()) {
			addNulls(list, indexNeeded - list.size() + 1);
		}
	}
	private void addNulls(List<String> list, int nulls) {
		for (int i = 0; i < nulls; i++) {
			list.add(null);
		}
	}
}
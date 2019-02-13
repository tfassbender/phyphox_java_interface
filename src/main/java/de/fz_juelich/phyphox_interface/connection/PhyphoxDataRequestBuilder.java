package de.fz_juelich.phyphox_interface.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A builder class (following the builder pattern) that is used to create a PhyphoxREquest object.
 */
public class PhyphoxDataRequestBuilder {
	
	private List<String> buffers;
	private List<String> offsets;
	private Map<int[], String> bufferOffsets;//used for offsets that have buffer values (e.g. buffer1=42|time)
	
	public PhyphoxDataRequestBuilder() {
		reset();
	}
	
	public PhyphoxDataRequest build() {
		replacePresetBufferOffsets();
		if (buffers.size() > offsets.size()) {
			expandListSize(offsets, buffers.size() - 1);
		}
		else if (buffers.size() < offsets.size()) {
			expandListSize(buffers, offsets.size() - 1);
		}
		return new PhyphoxDataRequest(buffers, offsets);
	}
	
	/**
	 * Add the texts for the buffer offsets.
	 */
	private void replacePresetBufferOffsets() {
		for (Entry<int[], String> bufferOffset : bufferOffsets.entrySet()) {
			String offsetText = bufferOffset.getValue() + "|" + buffers.get(bufferOffset.getKey()[1]);
			offsets.set(bufferOffset.getKey()[0], offsetText);
		}
	}
	
	public PhyphoxDataRequestBuilder setBuffer(int index, String name) {
		expandListSize(buffers, index);
		buffers.set(index, name);
		return this;
	}
	
	public PhyphoxDataRequestBuilder setOffset(int index, String offset) {
		expandListSize(offsets, index);
		offsets.set(index, offset);
		removeBufferOffsets(index);
		return this;
	}
	
	private void removeBufferOffsets(int index) {
		Iterator<Entry<int[], String>> iter = bufferOffsets.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<int[], String> entry = iter.next();
			if (entry.getKey()[0] == index) {
				iter.remove();
			}
		}
	}
	
	public PhyphoxDataRequestBuilder setOffset(int index, double offset) {
		return setOffset(index, Double.toString(offset));
	}
	
	public PhyphoxDataRequestBuilder setOffsetToBuffer(int index, int bufferIndex, String bufferOffset) {
		expandListSize(offsets, index);
		String offset = null;
		offsets.set(index, offset);
		removeBufferOffsets(index);//remove other bufferOffsets
		bufferOffsets.put(new int[] {index, bufferIndex}, bufferOffset);
		return this;
	}
	public PhyphoxDataRequestBuilder setOffsetToBuffer(int index, int bufferIndex, double bufferOffset) {
		return setOffsetToBuffer(index, bufferIndex, Double.toString(bufferOffset));
	}
	
	public void reset() {
		buffers = new ArrayList<String>();
		offsets = new ArrayList<String>();
		bufferOffsets = new HashMap<int[], String>();
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
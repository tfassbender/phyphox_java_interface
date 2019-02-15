package de.fz_juelich.phyphox_interface.data;

import java.util.List;

/**
 * An interface to be informed about changes in the data.
 */
public interface PhyphoxDataListener {
	
	public void updateData(List<PhyphoxBuffer> newData);
}
package de.fz_juelich.phyphox_interface.data;

import java.util.List;

import de.fz_juelich.phyphox_interface.connection.PhyphoxDataReceiver;

/**
 * The main class to be used.<br>
 * Using the PhyphoxData class one can crate a connection to the phone (in remote mode) and get the data from the phone in given time-intervals.<br>
 * The data is than transformed (usually into double values) to be used. Afterwards the user can get all recorded data or the new data.
 */
public class PhyphoxData<T> {
	
	private PhyphoxDataReceiver receiver;//the connection to the phone (parses JSON, ...)
	private List<List<T>> data;//all the data from the phone buffers
	private int buffers;//the number of buffers that are used (locally)
	private List<PhyphoxDataTransformer<T>> transformers;//transform the raw data into usable values (usually double)
	private double updateRate;//the update rate to request new data from the phone (in Hz)
	private List<Integer> lastRead;//the last indices of data that were read from the user
	
	public List<List<T>> getAllData() {
		//TODO
		return null;
	}
	
	public List<T> getBufferData(int buffer) {
		//TODO
		return null;
	}
	
	public List<List<T>> getNewData() {
		//TODO
		return null;
	}
	
	public List<T> getNewData(int buffer) {
		//TODO
		return null;
	}
}
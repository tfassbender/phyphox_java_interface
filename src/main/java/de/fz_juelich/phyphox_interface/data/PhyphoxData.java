package de.fz_juelich.phyphox_interface.data;

import java.util.ArrayList;
import java.util.List;

import de.fz_juelich.phyphox_interface.connection.PhyphoxConnection;
import de.fz_juelich.phyphox_interface.connection.PhyphoxDataCollector;

/**
 * The main class to be used.<br>
 * Using the PhyphoxData class one can crate a connection to the phone (in remote mode) and get the data from the phone in given time-intervals.<br>
 * The data is than transformed (usually into double values) to be used. Afterwards the user can get all recorded data or the new data.
 */
public class PhyphoxData {
	
	private PhyphoxDataCollector collector;//the connection to the phone (parses JSON, ...)
	private List<PhyphoxBuffer> data;//all the data from the phone buffers
	private int updateRate;//the update rate to request new data from the phone (in milliseconds)
	private int[] lastRead;//the last indices of data that were read from the user
	
	public PhyphoxData(PhyphoxConnection connection, List<String> bufferNames, int updateRate) {
		collector = new PhyphoxDataCollector(connection);
		data = new ArrayList<PhyphoxBuffer>(bufferNames.size());
		this.updateRate = updateRate;
		lastRead = new int[bufferNames.size()];
		for (int i = 0; i < bufferNames.size(); i++) {
			//initialize the last read indices with -1 to read all the data first
			lastRead[i] = -1;
			//create buffers
			data.add(new PhyphoxBuffer(bufferNames.get(i), new double[0]));
		}
	}
	
	public List<PhyphoxBuffer> getAllData() {
		return new ArrayList<PhyphoxBuffer>(data);
	}
	public PhyphoxBuffer getBufferData(String buffer) {
		return getBufferData(getBufferIndex(buffer));
	}
	public PhyphoxBuffer getBufferData(int buffer) {
		return data.get(buffer);
	}
	
	public List<PhyphoxBuffer> getNewData() {
		List<PhyphoxBuffer> newDataBuffers = new ArrayList<PhyphoxBuffer>(data.size());
		for (int i = 0; i < data.size(); i++) {
			newDataBuffers.add(getNewBufferData(i));
		}
		return newDataBuffers;
	}
	public PhyphoxBuffer getNewBufferData(String buffer) {
		return getNewBufferData(getBufferIndex(buffer));
	}
	public PhyphoxBuffer getNewBufferData(int buffer) {
		int startIndex = Math.max(0, lastRead[buffer]);
		PhyphoxBuffer newDataBuffer = data.get(buffer).getCopyFromIndex(startIndex);//create a buffer with only the new data
		lastRead[buffer] = data.get(buffer).getData().length - 1;//update the index
		return newDataBuffer;
	}
	
	private int getBufferIndex(String name) {
		int index = -1;
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).getName().equals(name)) {
				index = i;
			}
		}
		if (index == -1) {
			throw new IllegalStateException("A buffer with the name '" + name + "' doesn't exist.");
		}
		else {
			return index;
		}
	}
}
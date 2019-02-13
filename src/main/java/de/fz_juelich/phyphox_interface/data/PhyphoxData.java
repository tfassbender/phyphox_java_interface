package de.fz_juelich.phyphox_interface.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.common.annotations.VisibleForTesting;

import de.fz_juelich.phyphox_interface.connection.PhyphoxConnection;
import de.fz_juelich.phyphox_interface.connection.PhyphoxConnectionException;
import de.fz_juelich.phyphox_interface.connection.PhyphoxDataCollector;
import de.fz_juelich.phyphox_interface.connection.PhyphoxDataRequest;
import de.fz_juelich.phyphox_interface.connection.PhyphoxDataRequestBuilder;

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
	private Thread dataUpdateThread;//a thread that updates the data by sending request to the phones experiment
	private int continuesBufferIndex;//an (optional) index of a continues buffer (e.g. time) so that the data doesn't need to be fully updated
	private double lastContinuesBufferValue;//the last value of the continues buffer that can be used to only update the new data from the experiment
	
	/**
	 * Create a new PhyphoxData object to model the buffered data from the experiment in java.
	 * 
	 * @param connection
	 *        The connection parameters (ip and port of the phone)
	 * 
	 * @param bufferNames
	 *        The name of the buffers you want to get from the phone (that have to be the same names that the experiment knows)
	 * 
	 * @param continuesBufferName
	 *        A name of an (optional) continues buffer (e.g. time) so that the data doesn't need to be fully updated<br>
	 *        The buffer can be one of the names in the parameter bufferNames but doesn't need to.
	 * 
	 * @param updateRate
	 *        The rate with that the data is updated locally (in milliseconds)
	 */
	public PhyphoxData(PhyphoxConnection connection, List<String> bufferNames, String continuesBufferName, int updateRate) {
		Objects.requireNonNull(connection, "A null object is no valid connection.");
		if (bufferNames == null || bufferNames.isEmpty()) {
			throw new IllegalArgumentException("Buffer names are empty. The names of the buffers are needed to get the data from the experiment.");
		}
		if (updateRate <= 0) {
			throw new IllegalArgumentException("The update rate must be a value greater than zero.");
		}
		List<String> bufferNamesClone = new ArrayList<String>(bufferNames);
		collector = new PhyphoxDataCollector(connection);
		//add the continues buffer if it's not already in the bufferNames parameter
		if (continuesBufferName != null) {
			continuesBufferIndex = bufferNamesClone.indexOf(continuesBufferName);
			if (continuesBufferIndex == -1) {
				continuesBufferIndex = bufferNamesClone.size();
				bufferNamesClone.add(continuesBufferName);//add the continues buffer to the buffer names
			}
		}
		else {
			//no continues buffer is used
			continuesBufferIndex = -1;
		}
		data = new ArrayList<PhyphoxBuffer>(bufferNamesClone.size());
		this.updateRate = updateRate;
		lastRead = new int[bufferNamesClone.size()];
		for (int i = 0; i < bufferNamesClone.size(); i++) {
			//initialize the last read indices with -1 to read all the data first
			lastRead[i] = -1;
			//create buffers
			data.add(new PhyphoxBuffer(bufferNamesClone.get(i), new double[0]));
		}
		startUpdateThread();
	}
	/**
	 * A PhyphoxDataObject that doesn't update any data (just for testing).
	 */
	@VisibleForTesting
	protected PhyphoxData(String continuesBufferName, String... names) {
		List<String> bufferNames = new ArrayList<String>(Arrays.asList(names));
		boolean continuesBufferNameAdded = false;
		if (continuesBufferName != null) {
			continuesBufferIndex = bufferNames.indexOf(continuesBufferName);
			if (continuesBufferIndex == -1) {
				continuesBufferIndex = bufferNames.size();
				bufferNames.add(continuesBufferName);//add the continues buffer to the buffer names
				continuesBufferNameAdded = true;
			}
		}
		else {
			//no continues buffer is used
			continuesBufferIndex = -1;
		}
		data = new ArrayList<PhyphoxBuffer>(bufferNames.size());
		lastRead = new int[bufferNames.size()];
		for (int i = 0; i < bufferNames.size(); i++) {
			//initialize the last read indices with -1 to read all the data first
			lastRead[i] = -1;
			//create buffers
			data.add(new PhyphoxBuffer(bufferNames.get(i), new double[0]));
		}
		if (continuesBufferNameAdded) {
			bufferNames.remove(bufferNames.size() - 1);//remove continues buffer that was added before to prevent side effects
		}
	}
	
	private void startUpdateThread() {
		dataUpdateThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						//create a request to get the new data from the experiment
						PhyphoxDataRequest request = createRequestForNewData();
						//send the request and get the new data from the experiment
						List<PhyphoxBuffer> newData = collector.getData(request);
						//add the new data to the buffers
						addNewDataToBuffers(newData);
						//wait some time before the next update
						Thread.sleep(updateRate);
					}
					catch (InterruptedException ie) {
						//interrupt the thread again because the flag was removed when the exception was caught
						Thread.currentThread().interrupt();
					}
					catch (PhyphoxConnectionException e) {
						throw new RuntimeException("A problem occured while trying to read the data from the experiment.", e);
					}
				}
			}
		});
		dataUpdateThread.setDaemon(true);//set to daemon to be terminated automatically when the application terminates
		dataUpdateThread.start();
	}
	
	/**
	 * Restart the data update thread (e.g. after it crashed). When the PhyphoxData object is created the thread is started automatically.
	 */
	public void restartUpdateThread() {
		dataUpdateThread.interrupt();//interrupt the old thread if it's still running
		startUpdateThread();//start a new one
	}
	
	@VisibleForTesting
	protected PhyphoxDataRequest createRequestForNewData() {
		PhyphoxDataRequest request = null;
		PhyphoxDataRequestBuilder builder = new PhyphoxDataRequestBuilder();
		if (continuesBufferIndex != -1) {
			for (int i = 0; i < data.size(); i++) {
				builder.setBuffer(i, data.get(i).getName());
				builder.setOffsetToBuffer(i, continuesBufferIndex, lastContinuesBufferValue);
			}
			builder.setOffset(continuesBufferIndex, lastContinuesBufferValue);
			request = builder.build();
		}
		else {
			for (int i = 0; i < data.size(); i++) {
				builder.setBuffer(i, data.get(i).getName());
				//only full updates (added automatically)
			}
			request = builder.build();
		}
		return request;
	}
	
	@VisibleForTesting
	protected void addNewDataToBuffers(List<PhyphoxBuffer> newData) {
		if (continuesBufferIndex != -1) {
			//there is a continues buffer (e.g. time) -> just add the new data
			for (PhyphoxBuffer buffer : newData) {
				String name = buffer.getName();
				PhyphoxBuffer existingBuffer = getBufferData(name);
				existingBuffer.attachData(buffer.getData());
			}
		}
		else {
			//no continues buffer -> all data is fully updated
			for (PhyphoxBuffer buffer : newData) {
				int existingBufferIndex = getBufferIndex(buffer.getName());
				data.set(existingBufferIndex, buffer);
			}
		}
	}
	
	/**
	 * Get all buffers from the experiment.<br>
	 * The buffers are not cloned, so be careful when changing them.
	 */
	public List<PhyphoxBuffer> getAllData() {
		List<PhyphoxBuffer> allData = new ArrayList<PhyphoxBuffer>(data.size());
		for (int i = 0; i < data.size(); i++) {
			allData.add(getBufferData(i));
		}
		return allData;
	}
	/**
	 * Get a single buffer from the experiment by it's name.<br>
	 * The buffers are not cloned, so be careful when changing them.
	 */
	public PhyphoxBuffer getBufferData(String buffer) {
		return getBufferData(getBufferIndex(buffer));
	}
	/**
	 * Get a single buffer from the experiment by it's index.<br>
	 * The buffers are not cloned, so be careful when changing them.
	 */
	public PhyphoxBuffer getBufferData(int buffer) {
		lastRead[buffer] = data.get(buffer).size();
		return data.get(buffer);
	}
	
	/**
	 * Get every buffers new data (in form of a new buffer). The new data includes everything on from the last time the buffer was read.<br>
	 * The buffers are not cloned, so be careful when changing them.
	 */
	public List<PhyphoxBuffer> getNewData() {
		List<PhyphoxBuffer> newDataBuffers = new ArrayList<PhyphoxBuffer>(data.size());
		for (int i = 0; i < data.size(); i++) {
			newDataBuffers.add(getNewBufferData(i));
		}
		return newDataBuffers;
	}
	/**
	 * Get a single buffers new data (in form of a new buffer) by it's name. The new data includes everything on from the last time the buffer was
	 * read.<br>
	 * The buffers are not cloned, so be careful when changing them.
	 */
	public PhyphoxBuffer getNewBufferData(String buffer) {
		return getNewBufferData(getBufferIndex(buffer));
	}
	/**
	 * Get a single buffers new data (in form of a new buffer) by it's index. The new data includes everything on from the last time the buffer was
	 * read.<br>
	 * The buffers are not cloned, so be careful when changing them.
	 */
	public PhyphoxBuffer getNewBufferData(int buffer) {
		int startIndex = Math.max(0, lastRead[buffer]);
		PhyphoxBuffer newDataBuffer = data.get(buffer).getCopyFromIndex(startIndex);//create a buffer with only the new data
		lastRead[buffer] = data.get(buffer).getData().length - 1;//update the index
		return newDataBuffer;
	}
	
	/**
	 * Delete all the data from the buffers (except of the last value in the buffer to know what data is needed next from the experiment).
	 */
	public void clearAllBuffers() {
		for (int i = 0; i < data.size(); i++) {
			clearBuffer(i);
		}
	}
	/**
	 * Delete all the data from a single buffer, identified by it's name (except of the last value in the buffer to know what data is needed next from
	 * the experiment).
	 */
	public void clearBuffer(String buffer) {
		clearBuffer(getBufferIndex(buffer));
	}
	/**
	 * Delete all the data from a single buffer, identified by it's index (except of the last value in the buffer to know what data is needed next
	 * from the experiment).
	 */
	public void clearBuffer(int buffer) {
		PhyphoxBuffer fullBuffer = data.get(buffer);
		PhyphoxBuffer clearBuffer = new PhyphoxBuffer(fullBuffer.getName(), new double[0]);
		data.set(buffer, clearBuffer);
		lastRead[buffer] = -1;//reset the last read index
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
	
	@VisibleForTesting
	protected int getContinuesBufferIndex() {
		return continuesBufferIndex;
	}
}
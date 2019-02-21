package de.fz_juelich.phyphox_interface.connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import de.fz_juelich.phyphox_interface.data.PhyphoxBuffer;

/**
 * The connection to phyphox (via REST) is implemented here. The requests are sent to the phone (in remote mode) and the raw data is received in this
 * class. The data is not kept here but is just passed on (Transformed and usable data is to be found in the class PhyphoxData in the data package).
 */
public class PhyphoxConnection {
	
	private PhyphoxConnectionSettings connection;
	
	public PhyphoxConnection(PhyphoxConnectionSettings connection) {
		this.connection = connection;
	}
	
	/**
	 * Collect the data from the phone using a HTTP-GET request.<br>
	 * The request string is generated from the PhyphoxDataRequest object.
	 */
	public List<PhyphoxBuffer> getData(PhyphoxDataRequest request) throws PhyphoxConnectionException {
		return getData(request.getAsString());
	}
	
	/**
	 * Collect the data from the phone using a HTTP-GET request.<br>
	 * 
	 * For more information see:
	 * {@link https://github.com/Staacks/phyphox-android/blob/master/app/src/main/java/de/rwth_aachen/phyphox/remoteServer.java} (local class
	 * getCommandHandler)
	 */
	public List<PhyphoxBuffer> getData(String request) throws PhyphoxConnectionException {
		String basePath = connection.getUrlAsString();
		String resourcePath = "/get?" + request;
		
		String httpRequest = getHttpRequestString(basePath, resourcePath);
		
		List<PhyphoxBuffer> data;
		try {
			data = getDataFromRestServerApache(httpRequest);
			return data;
		}
		catch (ParseException | IOException e) {
			throw new PhyphoxConnectionException(
					"An error occured while trying to get the data from the phone. HTTP-Request was: \"" + httpRequest + "\"", e);
		}
	}
	
	/**
	 * Remote start the experiment on the phone.
	 */
	public void startExperiment() throws PhyphoxConnectionException {
		String basePath = connection.getUrlAsString();
		String resourcePath = "/control?cmd=start";
		String startExperimentRequest = getHttpRequestString(basePath, resourcePath); 
		try {
			String jsonResultString = getDataInJsonRepresentation(startExperimentRequest);
			boolean result = getResultFromJsonRepresentation(jsonResultString);
			if (!result) {
				//the starting of the experiment seems to be failed for unknown reasons
				throw new PhyphoxConnectionException(
						"The experiment couldn't be started. The phone's remote server sent a negative answer for unknown reasons.");
			}
		}
		catch (ParseException | IOException | JsonSyntaxException | IllegalArgumentException e) {
			throw new PhyphoxConnectionException("Problems occured while trying to start the experiment.", e);
		}
	}
	/**
	 * Remote stop the experiment on the phone.
	 */
	public void stopExperiment() throws PhyphoxConnectionException {
		String basePath = connection.getUrlAsString();
		String resourcePath = "/control?cmd=stop";
		String stopExperimentRequest = getHttpRequestString(basePath, resourcePath); 
		try {
			String jsonResultString = getDataInJsonRepresentation(stopExperimentRequest);
			boolean result = getResultFromJsonRepresentation(jsonResultString);
			if (!result) {
				//the stopping of the experiment seems to be failed for unknown reasons
				throw new PhyphoxConnectionException(
						"The experiment couldn't be stopped. The phone's remote server sent a negative answer for unknown reasons.");
			}
		}
		catch (ParseException | IOException | JsonSyntaxException e) {
			throw new PhyphoxConnectionException("Problems occured while trying to stop the experiment.", e);
		}
	}
	
	/**
	 * Delete all the data of this experiment on the phone.
	 */
	public void clearExperimentData() throws PhyphoxConnectionException {
		String basePath = connection.getUrlAsString();
		String resourcePath = "/control?cmd=clear";
		String clearExperimentRequest = getHttpRequestString(basePath, resourcePath);
		try {
			String jsonResultString = getDataInJsonRepresentation(clearExperimentRequest);
			boolean result = getResultFromJsonRepresentation(jsonResultString);
			if (!result) {
				//the deleting of the experiment's data seems to be failed for unknown reasons
				throw new PhyphoxConnectionException(
						"The experiment data couldn't be cleared. The phone's remote server sent a negative answer for unknown reasons.");
			}
		}
		catch (ParseException | IOException | JsonSyntaxException e) {
			throw new PhyphoxConnectionException("Problems occured while trying to clear the experiment's data.", e);
		}
	}
	/**
	 * Send an user given value from an input element to a buffer on the phone.<br>
	 * 
	 * For more information see:
	 * {@link https://github.com/Staacks/phyphox-android/blob/master/app/src/main/java/de/rwth_aachen/phyphox/remoteServer.java} (local class
	 * controlCommandHandler)
	 * 
	 * @param bufferName
	 *        The name of the buffer that gets the value.
	 * 
	 * @param value
	 *        The value that is to be added to the buffer.
	 */
	public void setExperimentBuffer(String bufferName, String value) throws PhyphoxConnectionException {
		String basePath = connection.getUrlAsString();
		String resourcePath = "/control?cmd=set&buffer=" + bufferName + "&value=" + value;
		String setExperimentBufferRequest = getHttpRequestString(basePath, resourcePath);
		try {
			String jsonResultString = getDataInJsonRepresentation(setExperimentBufferRequest);
			boolean result = getResultFromJsonRepresentation(jsonResultString);
			if (!result) {
				//the starting of the experiment seems to be failed for unknown reasons
				throw new PhyphoxConnectionException(
						"The buffer data could not be set. The phone's remote server sent a negative answer for unknown reasons.");
			}
		}
		catch (ParseException | IOException | JsonSyntaxException e) {
			throw new PhyphoxConnectionException("Problems occured while trying to pass the data to the experiment's buffer.", e);
		}
	}
	/**
	 * Trigger an element of the experiment on the phone.<br>
	 * 
	 * For more information see:
	 * {@link https://github.com/Staacks/phyphox-android/blob/master/app/src/main/java/de/rwth_aachen/phyphox/remoteServer.java} (local class
	 * controlCommandHandler)
	 * 
	 * @param elementId
	 *        The id of the element that is triggered.
	 */
	public void triggerExperimentElement(String elementId) throws PhyphoxConnectionException {
		String basePath = connection.getUrlAsString();
		String resourcePath = "/control?cmd=trigger&element=" + elementId;
		String triggerExperimentElementRequest = getHttpRequestString(basePath, resourcePath);
		try {
			String jsonResultString = getDataInJsonRepresentation(triggerExperimentElementRequest);
			boolean result = getResultFromJsonRepresentation(jsonResultString);
			if (!result) {
				//the starting of the experiment seems to be failed for unknown reasons
				throw new PhyphoxConnectionException(
						"The element could not be triggered. The phone's remote server sent a negative answer for unknown reasons.");
			}
		}
		catch (ParseException | IOException | JsonSyntaxException e) {
			throw new PhyphoxConnectionException("Problems occured while trying trigger the element.", e);
		}
	}
	
	/**
	 * Collect the data from the phone using a HTTP-GET request and parse the data into a PhyphoxBuffer object.
	 */
	private List<PhyphoxBuffer> getDataFromRestServerApache(String httpRequest) throws ParseException, IOException {
		String jsonRepresentation = getDataInJsonRepresentation(httpRequest);
		List<PhyphoxBuffer> data = getBufferDataFromJsonRepresentation(jsonRepresentation);
		return data;
	}
	
	private String getHttpRequestString(String basePath, String resourcePath) {
		String encodedUrl = "http://" + basePath + resourcePath;
		//replace characters that seem to be not allowed in a java url
		encodedUrl = encodedUrl.replace("|", "%7C");
		encodedUrl = encodedUrl.replaceAll(" ", "%20");
		return encodedUrl;
	}
	
	/**
	 * Get the data from the phone in JSON representation using an Apache HTTP-Client<br>
	 * 
	 * Tutorial from: https://howtodoinjava.com/httpclient/jaxrs-client-httpclient-get-post/
	 */
	private String getDataInJsonRepresentation(String request) throws ParseException, IOException {
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		try {
			//Define a HttpGet request; You can choose between HttpPost, HttpDelete or HttpPut also.
			//Choice depends on type of method you will be invoking.
			
			//System.out.println("Generating HTTP-GET request:\n" + request);
			HttpGet getRequest = new HttpGet(request);
			
			//Set the API media type in http accept header (it always returns json for there is nothing else but...)
			getRequest.addHeader("accept", "application/json");
			
			//Send the request; It will immediately return the response in HttpResponse object
			HttpResponse response = httpClient.execute(getRequest);
			
			//verify the valid error code first
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				throw new RuntimeException("Get data from experiment failed with HTTP error code : " + statusCode);
			}
			
			//Now pull back the response object
			HttpEntity httpEntity = response.getEntity();
			String apiOutput = EntityUtils.toString(httpEntity);
			
			return apiOutput;
		}
		finally {
			//Important: Close the connect
			httpClient.getConnectionManager().shutdown();
		}
	}
	
	/**
	 * Get the buffer data from the JSON representation using Google's 'gson' API:<br>
	 * https://github.com/google/gson
	 * 
	 * Tutorial from: <br>
	 * http://blog.mynotiz.de/programmieren/java-json-decode-tutorial-2074/ <br>
	 * https://github.com/google/gson/blob/master/UserGuide.md
	 */
	private List<PhyphoxBuffer> getBufferDataFromJsonRepresentation(String jsonRepresentation) {
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>();
		Gson gson = new Gson();
		
		JsonObject json = gson.fromJson(jsonRepresentation, JsonObject.class);
		JsonObject allBuffers = json.getAsJsonObject("buffer");
		
		for (Entry<String, JsonElement> buffer : allBuffers.entrySet()) {
			String bufferName = buffer.getKey();
			JsonObject value = buffer.getValue().getAsJsonObject();
			JsonArray bufferDataAsArray = value.getAsJsonArray("buffer");
			double[] bufferData = new double[bufferDataAsArray.size()];
			for (int i = 0; i < bufferDataAsArray.size(); i++) {
				bufferData[i] = bufferDataAsArray.get(i).getAsDouble();
			}
			
			PhyphoxBuffer phyBuffer = new PhyphoxBuffer(bufferName, bufferData);
			buffers.add(phyBuffer);
		}
		
		return buffers;
	}
	
	private boolean getResultFromJsonRepresentation(String jsonRepresentation) {
		//first replace all '=' characters with ':' (because the returned data is no correct JSON at the moment)
		jsonRepresentation = jsonRepresentation.replaceAll("=", ":");
		
		//after the JSON notation is repaired, read the result using Google's Gson-API
		Gson gson = new Gson();
		JsonObject json = gson.fromJson(jsonRepresentation, JsonObject.class);
		boolean result = json.get("result").getAsBoolean();
		
		return result;
	}
}
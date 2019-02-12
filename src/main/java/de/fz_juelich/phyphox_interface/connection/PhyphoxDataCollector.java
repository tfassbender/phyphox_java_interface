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

import de.fz_juelich.phyphox_interface.data.PhyphoxBuffer;

/**
 * The connection to phyphox (via REST) is implemented here. The requests are sent to the phone (in remote mode) and the raw data is received in this
 * class. The data is not kept here but is just passed on (Transformed and usable data is to be found in the class PhyphoxData in the data package).
 */
public class PhyphoxDataCollector {
	
	private PhyphoxConnection connection;
	
	public PhyphoxDataCollector(PhyphoxConnection connection) {
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
	 * Collect the data from the phone using a HTTP-GET request.
	 */
	public List<PhyphoxBuffer> getData(String request) throws PhyphoxConnectionException {
		String basePath = connection.getUrlAsString();
		String resourcePath = "/get?" + request;
		
		List<PhyphoxBuffer> data;
		try {
			data = getDataFromRestServerApache(basePath, resourcePath);
			return data;
		}
		catch (ParseException | IOException e) {
			throw new PhyphoxConnectionException("An error occured while trying to get the data from the phone.", e);
		}
	}
	
	/**
	 * Collect the data from the phone using a HTTP-GET request and parse the data into a PhyphoxBuffer object.
	 */
	private List<PhyphoxBuffer> getDataFromRestServerApache(String basePath, String resourcePath) throws ParseException, IOException {
		String jsonRepresentation = getDataInJsonRepresentation(basePath, resourcePath);
		List<PhyphoxBuffer> data = getBufferDataFromJsonRepresentation(jsonRepresentation);
		return data;
	}
	
	/**
	 * Get the data from the phone in JSON representation using an Apache HTTP-Client<br>
	 * 
	 * Tutorial from: https://howtodoinjava.com/httpclient/jaxrs-client-httpclient-get-post/
	 */
	private String getDataInJsonRepresentation(String basePath, String resourcePath) throws ParseException, IOException {
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		try {
			//Define a HttpGet request; You can choose between HttpPost, HttpDelete or HttpPut also.
			//Choice depends on type of method you will be invoking.
			HttpGet getRequest = new HttpGet(basePath + resourcePath);
			
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
	
	/**
	 * Unused at the moment for Jersey won't seem to work but gives HTTP error 501 (not implemented on server side)
	 */
	/*private PhyphoxRawData getDataFromRestServer(Client client, String basePath, String resourcePath) {
		//from Book: "Der Java Profi: Persistenzlösungen und REST-Services" p. 277
		WebTarget webTarget = client.target(basePath).path(resourcePath);
		System.out.println("Sending GET request to URL '" + basePath + resourcePath + "'");
		
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		int responseCode = response.getStatus();
		
		System.out.println("Response Code: " + responseCode);
		
		if (responseCode != Response.Status.OK.getStatusCode()) {
			throw new RuntimeException("HTTP error code: " + responseCode);
		}
		else if (response.hasEntity()) {
			String responseText = response.readEntity(String.class);
			System.out.println(responseText);
		}
		return null;
	}*/
}
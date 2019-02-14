package de.fz_juelich.phyphox_interface.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import de.fz_juelich.phyphox_interface.connection.PhyphoxConnection;
import de.fz_juelich.phyphox_interface.data.PhyphoxBuffer;
import de.fz_juelich.phyphox_interface.data.PhyphoxData;
import de.fz_juelich.phyphox_interface.data.PhyphoxDataListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * A simple example implementation to show how the phyphox_java_interface can be used (using a text-area and a plot for visualization)
 */
public class PhyphoxInterfaceVisualisationController implements Initializable, PhyphoxDataListener {
	
	@FXML
	private TextField textFieldPhyphoxIp;
	@FXML
	private TextField textFieldPhyphoxPort;
	@FXML
	private TextField textFieldNameBuffer1;
	@FXML
	private TextField textFieldTimeBuffer;
	@FXML
	private Button buttonReadBuffers;
	
	@FXML
	private TextArea textAreaOutputPlainText;
	@FXML
	private LineChart<String, Double> chartOutputData;
	
	private PhyphoxData experimentData;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		buttonReadBuffers.setOnAction(e -> startStopExperimentReader());
	}
	
	private void startStopExperimentReader() {
		if (experimentData == null) {
			//no experiment reader -> start the reader
			
			//read the IP and the port the user entered
			String ip = textFieldPhyphoxIp.getText();
			int port;
			try {
				port = Integer.parseInt(textFieldPhyphoxPort.getText());
			}
			catch (NumberFormatException nfe) {
				//if the port can't be interpreted use the default port 8080
				port = 8080;
			}
			//a connection object with an IP and a port
			PhyphoxConnection connection = new PhyphoxConnection(ip, port);
			//the buffer names (can include the continues buffer but doesn't need to)
			List<String> bufferNames = Arrays.asList(new String[] {textFieldNameBuffer1.getText()});
			//a continues buffer like the time (optional; null if no one exists)
			String continuesBufferName = textFieldTimeBuffer.getText();
			//update data from the experiment once a second
			int updateRate = 1000;
			
			//initialize the textArea and the chart before starting the data update
			initializeTextArea();
			initializeChart();
			
			//create the PhyphoxData object that starts reading the buffers automatically
			experimentData = new PhyphoxData(connection, bufferNames, continuesBufferName, updateRate);
			//register this object as PhyphoxDataListener to be informed about new data
			experimentData.addDataListener(this);
			
			//change the button text
			buttonReadBuffers.setText("Stop Reader");
		}
		else {
			//reader already running -> stop the reader
			
			//the data collector needs to be stopped before deleting it because it has a thread that would stay alive
			experimentData.stop();
			//after stopping it the reference can be set to null and the Java-GC does the rest
			experimentData = null;
			
			//change the button text
			buttonReadBuffers.setText("Start Reader");
		}
	}
	
	private void initializeTextArea() {
		//delete all the old data
		textAreaOutputPlainText.clear();
		textAreaOutputPlainText.setText("Time      -      Value");
	}
	
	private void initializeChart() {
		//clear all old data
		chartOutputData.getData().clear();
		//add a new data series where the new data can be appended
		XYChart.Series<String, Double> series = new XYChart.Series<String, Double>();
		chartOutputData.getData().add(series);
		chartOutputData.setAnimated(false);
	}
	
	@Override
	public void updateData(List<PhyphoxBuffer> newData, boolean fullUpdate) {
		if (fullUpdate) {
			//would be the case if there would be no continues buffer (like the time buffer)
			//the data would not be appended at the end but fully updated...
			
			//here only some printing is implemented for test reasons
			for (PhyphoxBuffer buffer : newData) {
				System.out.println("\n" + buffer.getName());
				double[] data = buffer.getData();
				for (int i = 0; i < data.length; i++) {
					System.out.println(i + " " + data[i]);
				}
			}
		}
		else {
			//should be the case in this example because there is a continues buffer (the time buffer)
			
			//just add the new data at the end
			if (newData.size() == 2) {//should be x-values and time
				//we can use index 0 here because it was the first name that was added 
				PhyphoxBuffer xDataBuffer = newData.get(0);
				
				//alternatively just check the names of the buffers
				String timeBufferName = textFieldTimeBuffer.getText();
				Optional<PhyphoxBuffer> timeBufferOptional = PhyphoxBuffer.getByName(newData, timeBufferName);
				PhyphoxBuffer timeBuffer;
				//get the buffer if there was any with the searched name
				if (timeBufferOptional.isPresent()) {
					timeBuffer = timeBufferOptional.get();
				}
				else {
					//if there is no such buffer something went wrong and should probably be logged or something...
					return;
				}
				
				//TODO print only for testing
				for (PhyphoxBuffer buffer : newData) {
					System.out.println("\n" + buffer.getName());
					double[] data = buffer.getData();
					for (int i = 0; i < data.length; i++) {
						System.out.println(i + " " + data[i]);
					}
				}
				
				//append the data to the text area and the chart
				List<XYChart.Data<String, Double>> chartDataPoints = new ArrayList<XYChart.Data<String, Double>>();
				for (int i = 0; i < newData.get(0).size(); i++) {
					double time = timeBuffer.getData()[i];
					double xData = xDataBuffer.getData()[i];
					textAreaOutputPlainText.appendText(String.format("\n%.3f              %.5f", time, xData));
					chartDataPoints.add(new XYChart.Data<String, Double>(String.format("%.4f", time), xData));
				}
				//append at the end of the chart data series (needs to be run in an JavaFX thread; therefore the Platform.runLater())
				Platform.runLater(() -> chartOutputData.getData().get(0).getData().addAll(chartDataPoints));
			}
		}
	}
}
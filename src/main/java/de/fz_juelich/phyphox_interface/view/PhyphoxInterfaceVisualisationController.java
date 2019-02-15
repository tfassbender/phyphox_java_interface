package de.fz_juelich.phyphox_interface.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import de.fz_juelich.phyphox_interface.connection.PhyphoxConnectionException;
import de.fz_juelich.phyphox_interface.connection.PhyphoxConnectionSettings;
import de.fz_juelich.phyphox_interface.data.PhyphoxBuffer;
import de.fz_juelich.phyphox_interface.data.PhyphoxExperiment;
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
	private Button buttonStartExperiment;
	@FXML
	private Button buttonStopExperiment;
	@FXML
	private Button buttonClearData;
	
	@FXML
	private TextArea textAreaOutputPlainText;
	@FXML
	private LineChart<String, Double> chartOutputData;
	
	private PhyphoxExperiment experiment;
	
	public PhyphoxInterfaceVisualisationController() {
		//don't create the experiment here because we don't know the names of the buffers yet.
		//(only in this example... if you already know the names you can create it here)
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//set the button actions
		buttonStartExperiment.setOnAction(e -> startExperiment());
		buttonStopExperiment.setOnAction(e -> stopExperiment());
		buttonClearData.setOnAction(e -> clearExperimentData());
		
		//configure the plot
		chartOutputData.setCreateSymbols(false);
		chartOutputData.setAnimated(false);
		chartOutputData.setTitle("Phyphox Experiment Data");
	}
	
	private void startExperiment() {
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
		PhyphoxConnectionSettings connection = new PhyphoxConnectionSettings(ip, port);
		//the buffer names (can include the continues buffer but doesn't need to)
		List<String> bufferNames = Arrays.asList(new String[] {textFieldNameBuffer1.getText(), textFieldTimeBuffer.getText()});
		//update data from the experiment every second
		int updateRate = 1000;
		
		//initialize the textArea and the chart before starting the data update
		initializeTextArea();
		initializeChart();
		
		//if there is an old experiment running try to stop it first
		if (experiment != null) {
			try {
				experiment.stopExperiment();
			}
			catch (PhyphoxConnectionException pce) {
				//can be ignored here; it will probably cause an error when starting the new experiment which is more important
			}
		}
		//create the PhyphoxExperiment object that starts reading the buffers automatically
		experiment = new PhyphoxExperiment(connection, bufferNames, updateRate);
		//register this object as PhyphoxDataListener to be informed about new data
		experiment.addDataListener(this);
		//start the experiment
		try {
			experiment.startExperiment();
		}
		catch (PhyphoxConnectionException pce) {
			//when something goes wrong, just give a message on the screen
			DialogUtils.showExceptionDialog("Problems while starting the experiment", pce.getMessage(), pce);
			//in a real application the error should probably be handled
		}
	}
	
	private void stopExperiment() {
		if (experiment != null) {
			//stop the experiment if there is one
			try {
				experiment.stopExperiment();
			}
			catch (PhyphoxConnectionException pce) {
				//when something goes wrong, just give a message on the screen
				DialogUtils.showExceptionDialog("Problems while stopping the experiment", pce.getMessage(), pce);
				//in a real application the error should probably be handled
			}
		}
	}
	
	private void clearExperimentData() {
		if (experiment != null) {
			//clear the data from the experiment if there is one
			try {
				experiment.clearExperimentData();
			}
			catch (PhyphoxConnectionException pce) {
				//when something goes wrong, just give a message on the screen
				DialogUtils.showExceptionDialog("Problems while clearing the experiment's data", pce.getMessage(), pce);
				//in a real application the error should probably be handled
			}
		}
		//clear the text area and the plot
		textAreaOutputPlainText.clear();
		chartOutputData.getData().clear();
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
		series.setName("Some fancy phyphox data");
		chartOutputData.getData().add(series);
	}
	
	@Override
	public void updateData(List<PhyphoxBuffer> newData) {
		//print the data for testing:
		//printData(newData);
		
		List<XYChart.Data<String, Double>> chartDataPoints = getAsChartData(newData);
		
		//set the chart data series (needs to be run in an JavaFX thread; therefore the Platform.runLater())
		Platform.runLater(() -> {
			chartOutputData.getData().clear();
			XYChart.Series<String, Double> series = new XYChart.Series<String, Double>();
			series.getData().addAll(chartDataPoints);
			series.setName("Some fancy phyphox data");
			chartOutputData.getData().add(series);
		});
	}
	
	private List<XYChart.Data<String, Double>> getAsChartData(List<PhyphoxBuffer> newData) {
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
				return null;
			}
			
			//append the data to the text area and the chart
			List<XYChart.Data<String, Double>> chartDataPoints = new ArrayList<XYChart.Data<String, Double>>();
			//there may be differences in the number of values in every array, so use the lower number of values
			int numData = Math.min(newData.get(0).size(), newData.get(1).size());
			
			//use a maximum number of points in the plot (otherwise these FX-Plots will will get really slow). 
			int maxDataPoints = 1000;
			int displayedRestClass;
			if (numData > maxDataPoints) {
				displayedRestClass = numData / maxDataPoints;
			}
			else {
				displayedRestClass = 1;//display all
			}
			
			//also add the values to the text area
			StringBuilder sb = new StringBuilder();
			sb.append("Time      -      Value");
			
			for (int i = 0; i < numData; i++) {
				//only display some of the measured points
				if (i % displayedRestClass == 0) {
					
					double time = timeBuffer.getData()[i];
					double xData = xDataBuffer.getData()[i];
					
					sb.append(String.format("\n%.3f              %.5f", time, xData));
					chartDataPoints.add(new XYChart.Data<String, Double>(String.format("%.4f", time), xData));
				}
			}
			textAreaOutputPlainText.setText(sb.toString());
			
			return chartDataPoints;
		}
		else {
			//there seems to be problems with the data (that should probably be logged)
			return null;
		}
	}
	
	@SuppressWarnings("unused")
	private void printData(List<PhyphoxBuffer> newData) {
		for (PhyphoxBuffer buffer : newData) {
			System.out.println("\n" + buffer.getName());
			double[] data = buffer.getData();
			for (int i = 0; i < data.length; i++) {
				System.out.println(i + " " + data[i]);
			}
		}
	}
}
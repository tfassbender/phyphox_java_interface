package de.fz_juelich.phyphox_interface.view;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PhyphoxInterfaceVisualisationController implements Initializable {
	
	@FXML
	private TextField textFieldPhyphoxIp;
	@FXML
	private TextField textFieldPhyphoxPort;
	@FXML
	private TextField textFieldNameBuffer1;
	@FXML
	private TextField textFieldNameBuffer2;
	@FXML
	private Button buttonReadBuffers;
	
	@FXML
	private TextArea textAreaOutputPlainText;
	@FXML
	private LineChart<?, ?> chartOutputData;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}
}
package de.fz_juelich.phyphox_interface.view;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PhyphoxInterfaceVisualisationApplication extends Application {
	
	private PhyphoxInterfaceVisualisationController controller;
	
	public static final String APPLICATION_NAME = "Phyphox Java Interface Visualizer";
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			URL fxmlUrl = getClass().getResource("PhyphoxInterfaceVisualisation.fxml");
			FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
			controller = new PhyphoxInterfaceVisualisationController();
			fxmlLoader.setController(controller);
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root, 800, 600);
			primaryStage.setScene(scene);
			primaryStage.setTitle(APPLICATION_NAME);
			primaryStage.show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class SimulatorGUI extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main_view.fxml"));
		Parent root = fxmlLoader.load();

		stage.setScene(new Scene(root));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
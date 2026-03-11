package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class SimulatorGUI extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		final double stageHeight = 600;
		final double stageWidth = 800;

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main_view.fxml"));
		Parent root = fxmlLoader.load();

		Scene scene = new Scene(root);
		scene.getStylesheets().add("Style.css");

		Image icon = new Image("walrus_main.png");
		scene.getStylesheets().add("Style.css");
		stage.setScene(scene);

		stage.getIcons().add(icon);
		stage.setTitle("Mursu Wallstreet");

		stage.setHeight(stageHeight);
		stage.setMinHeight(stageHeight);
		stage.setWidth(stageWidth);
		stage.setMinWidth(stageWidth);

		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
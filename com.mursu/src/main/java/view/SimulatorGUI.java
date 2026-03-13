package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

/**
 * JavaFX application entry point for the simulator user interface.
 *
 * <p>This class boots the desktop UI, loads the main FXML layout, applies the
 * stylesheet and window icon, and configures a fixed minimum window size for
 * the simulator.</p>
 */
public class SimulatorGUI extends Application {

	/**
	 * Builds and shows the primary JavaFX stage for the simulator.
	 *
	 * @param stage the primary application window provided by the JavaFX runtime
	 * @throws Exception if the main FXML view or its resources cannot be loaded
	 */
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

	/**
	 * Launches the JavaFX application.
	 *
	 * @param args standard command-line arguments passed to JavaFX
	 */
	public static void main(String[] args) {
		launch(args);
	}
}

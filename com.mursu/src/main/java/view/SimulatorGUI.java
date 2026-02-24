package view;

import java.text.DecimalFormat;
import controller.*;
import eds.framework.Trace;
import eds.framework.Trace.Level;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import java.text.DecimalFormat;

public class SimulatorGUI extends Application implements ISimulatorUI {

	// Controller object (UI needs)
	private IControllerVtoM controller;

	// UI Components:
	private TextField time;
	private TextField delay;
	private Label results;
	private Label timeLabel;
	private Label delayLabel;
	private Label resultLabel;
	
	private Button startButton;
	private Button slowButton;
	private Button speedUpButton;

	private IVisualisation display;


	@Override
	public void init() {
		Trace.setTraceLevel(Level.INFO);
		controller = new Controller(this);
	}

	@Override
	public void start(Stage primaryStage) {
		// UI creation
		try {
			primaryStage.setOnCloseRequest(t -> {
                Platform.exit();
                System.exit(0);
            });

			primaryStage.setTitle("Simulator");

			startButton = new Button();
			startButton.setText("Start simulation");
			startButton.setOnAction(event -> {
                controller.startSimulation();
                startButton.setDisable(true);
            });

			slowButton = new Button();
			slowButton.setText("Slow down");
			slowButton.setOnAction(e -> controller.decreaseSpeed());

			speedUpButton = new Button();
			speedUpButton.setText("Speed up");
			speedUpButton.setOnAction(e -> controller.increaseSpeed());

			timeLabel = new Label("Simulation time:");
			timeLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
	        time = new TextField("Give time");
	        time.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
	        time.setPrefWidth(150);

	        delayLabel = new Label("Delay:");
			delayLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
	        delay = new TextField("Give delay");
	        delay.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
	        delay.setPrefWidth(150);
	                	        
	        resultLabel = new Label("Total time:");
			resultLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
	        results = new Label();
	        results.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
	        results.setPrefWidth(150);

	        HBox hBox = new HBox();
	        hBox.setPadding(new Insets(15, 12, 15, 12)); // margins up, right, bottom, left
	        hBox.setSpacing(10);   // node distance 10 pixel
	        
	        GridPane grid = new GridPane();
	        grid.setAlignment(Pos.CENTER);
	        grid.setVgap(10);
	        grid.setHgap(5);

	        grid.add(timeLabel, 0, 0);   // column, row
	        grid.add(time, 1, 0);
	        grid.add(delayLabel, 0, 1);
	        grid.add(delay, 1, 1);
	        grid.add(resultLabel, 0, 2);
	        grid.add(results, 1, 2);
	        grid.add(startButton,0, 3);
	        grid.add(speedUpButton, 0, 4);
	        grid.add(slowButton, 1, 4);
	        
	        display = new Visualisation2(400,200);
			//display = new Visualisation2(400,200);

	        // Fill the box:
	        hBox.getChildren().addAll(grid, (Canvas) display);
	        
	        Scene scene = new Scene(hBox);
	        primaryStage.setScene(scene);
	        primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/* UI interface methods (controller calls) */
	@Override
	public double getTime(){
		return Double.parseDouble(time.getText());
	}

	@Override
	public long getDelay(){
		return Long.parseLong(delay.getText());
	}

	@Override
	public void setEndingTime(double time) {
		 DecimalFormat formatter = new DecimalFormat("#0.00");
		 this.results.setText(formatter.format(time));
	}

	@Override
	public IVisualisation getVisualisation() {
		 return display;
	}

	/* JavaFX-application (UI) start-up */
	public static void main(String[] args) {
		launch(args);
	}
}

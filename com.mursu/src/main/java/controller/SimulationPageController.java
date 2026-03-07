package controller;

import eds.config.SimulationConfig;
import eds.database.Records.StatisticsAndMetricsRecord;
import eds.model.OrderBook;
import eds.model.StatisticsCollector;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controls the simulation page of the application.
 * This class handles simulation page buttons, labels, queue text,
 * order book table updates, and opening the results page.
 */
public class SimulationPageController {

    // These fields are linked to elements from simulation_view.fxml.
    @FXML private Label labelTimer;
    @FXML private Label labelArrival;
    @FXML private Label labelMarket;
    @FXML private Label labelLimit;
    @FXML private Label labelExecution;
    @FXML private Button btnPause;
    @FXML private Canvas graph;
    @FXML private TableView<String[]> orderBookTable;
    @FXML private TableColumn<String[], String> colAskShare;
    @FXML private TableColumn<String[], String> colAskPrice;
    @FXML private TableColumn<String[], String> colBidShare;
    @FXML private TableColumn<String[], String> colBidPrice;
    @FXML private TableColumn<String[], String> colSpread;

    // This logic controller is created in initialize() and used by this page.
    private Controller controller;

    // startSimulation(...) saves the selected simulation time limit here
    // so the timer label does not show a value above that limit.
    private double simulationTimeLimit;

    @FXML
    private void initialize() {
        // FXMLLoader in MainPageController.handleStartSimulation() loads simulation_view.fxml,
        // creates SimulationPageController, and then calls this method.
        // This method connects table columns to values in each String[] row.
        colAskShare.setCellValueFactory(rowData -> new SimpleStringProperty(rowData.getValue()[0]));
        colAskPrice.setCellValueFactory(rowData -> new SimpleStringProperty(rowData.getValue()[1]));
        colBidShare.setCellValueFactory(rowData -> new SimpleStringProperty(rowData.getValue()[2]));
        colBidPrice.setCellValueFactory(rowData -> new SimpleStringProperty(rowData.getValue()[3]));
        colSpread.setCellValueFactory(rowData -> new SimpleStringProperty(rowData.getValue()[4]));

        // The order book table starts empty before the first engine updates arrive.
        orderBookTable.setItems(FXCollections.observableArrayList());

        // This creates the logic controller used only by this simulation page.
        controller = new Controller(this);
    }

    /**
     * Starts the simulation page with the selected configuration.
     *
     * @param config the configuration values chosen on the main page
     */
    public void startSimulation(SimulationConfig config) {
        // MainPageController.handleStartSimulation() calls this method.
        // It saves the selected time limit and tells the logic controller to start MyEngine.
        simulationTimeLimit = config.simulationTime();
        controller.startSimulation(config);
    }

    @FXML
    private void handleSlowDown() {
        // The Slow Down button in simulation_view.fxml calls this method.
        // It forwards the action to Controller.decreaseSpeed().
        controller.decreaseSpeed();
    }

    @FXML
    private void handleSpeedUp() {
        // The Speed Up button in simulation_view.fxml calls this method.
        // It forwards the action to Controller.increaseSpeed().
        controller.increaseSpeed();
    }

    @FXML
    private void handlePause() {
        // The Pause button in simulation_view.fxml calls this method.
        // It forwards the action to Controller.togglePause().
        controller.togglePause();
    }

    /**
     * Changes the text on the pause button.
     *
     * @param text the new button text
     */
    public void setPauseButtonText(String text) {
        // Controller.togglePause() uses this method to change the button text.
        btnPause.setText(text);
    }

    /**
     * Updates the timer label on the simulation page.
     *
     * @param time the current simulation time from the engine
     */
    public void updateSimulationTime(double time) {
        // Controller.updateTimeAndQueues() uses this method to refresh the timer label.
        double shownTime = Math.min(time, simulationTimeLimit);
        labelTimer.setText(String.format("%.2f", shownTime));
    }

    /**
     * Updates the four queue labels on the simulation page.
     *
     * @param queueLengths queue values for validation, market, limit, and execution
     */
    public void updateQueueLengths(int[] queueLengths) {
        // Controller.updateTimeAndQueues() uses this method to refresh the four queue labels.
        if (queueLengths == null || queueLengths.length < 4) {
            return;
        }

        labelArrival.setText(String.valueOf(queueLengths[0]));
        labelMarket.setText(String.valueOf(queueLengths[1]));
        labelLimit.setText(String.valueOf(queueLengths[2]));
        labelExecution.setText(String.valueOf(queueLengths[3]));
    }

    /**
     * Shows one order book snapshot in the table on the simulation page.
     *
     * @param snapshot the current order book snapshot from the engine
     */
    public void showOrderBook(OrderBook.OrderBookSnapshot snapshot) {
        // Controller.updateOrderBook() uses this method after MyEngine sends a new snapshot.
        // It converts one order book snapshot into rows for the table on this page.
        if (snapshot == null) {
            return;
        }

        int rowCount = Math.max(snapshot.asks().size(), snapshot.bids().size());
        javafx.collections.ObservableList<String[]> rows = FXCollections.observableArrayList();
        String spreadText = "";

        if (snapshot.spread().isPresent()) {
            spreadText = String.format("%.4f", snapshot.spread().getAsDouble());
        }

        for (int i = 0; i < rowCount; i++) {
            OrderBook.PriceLevel ask = null;
            OrderBook.PriceLevel bid = null;
            String askShare = "";
            String askPrice = "";
            String bidShare = "";
            String bidPrice = "";
            String rowSpread = "";

            if (i < snapshot.asks().size()) {
                ask = snapshot.asks().get(i);
                askShare = String.valueOf(ask.totalQty());
                askPrice = String.format("%.4f", ask.price());
            }

            if (i < snapshot.bids().size()) {
                bid = snapshot.bids().get(i);
                bidShare = String.valueOf(bid.totalQty());
                bidPrice = String.format("%.4f", bid.price());
            }

            if (i == 0) {
                rowSpread = spreadText;
            }

            rows.add(new String[] { askShare, askPrice, bidShare, bidPrice, rowSpread });
        }

        orderBookTable.setItems(rows);
    }

    /**
     * Opens the results page and sends final simulation data there.
     *
     * @param snapshot the final simulation snapshot
     * @param record the final database record with metrics
     */
    public void showResultsPage(StatisticsCollector.Snapshot snapshot, StatisticsAndMetricsRecord record) {
        // Controller.showEndTime() calls this method after the run finishes.
        // It opens results_view.fxml and sends final simulation data to that page.
        try {
            // FXMLLoader reads results_view.fxml and builds the results page.
            // loader.load() also creates ResultsPageController from that FXML file.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/results_view.fxml"));
            Parent root = loader.load();
            ResultsPageController resultsController = loader.getController();
            resultsController.setResults(snapshot, record);

            // This takes the current simulation window and replaces it with the results page.
            Stage stage = (Stage) graph.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open results page", e);
        }
    }
}

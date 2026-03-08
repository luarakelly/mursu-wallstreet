package controller;

import eds.config.SimulationConfig;
import eds.database.Records.StatisticsAndMetricsRecord;
import eds.model.OrderBook;
import eds.model.StatisticsCollector;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * JavaFX controller for the simulation page.
 *
 * Responsibilities:
 * - connect controls from simulation_view.fxml to Java code
 * - forward user commands such as pause and speed changes to {@link Controller}
 * - expose UI update methods for timer, queues, and order book data
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

    private IViewToModelController controller;

    /*
    startSimulation() saves the selected simulation time limit here
    so the timer label does not show a value above that limit.
     */
    private double simulationTimeLimit;

    /**
     * Initializes the simulation page after the FXML file is loaded.
     * Sets up the order book table and creates the controller for this page.
     */
    @FXML
    private void initialize() {
        colAskShare.setCellValueFactory(rowData -> new SimpleStringProperty(rowData.getValue()[0]));
        colAskPrice.setCellValueFactory(rowData -> new SimpleStringProperty(rowData.getValue()[1]));
        colBidShare.setCellValueFactory(rowData -> new SimpleStringProperty(rowData.getValue()[2]));
        colBidPrice.setCellValueFactory(rowData -> new SimpleStringProperty(rowData.getValue()[3]));
        colSpread.setCellValueFactory(rowData -> new SimpleStringProperty(rowData.getValue()[4]));

        orderBookTable.setItems(FXCollections.observableArrayList());
        controller = new Controller(this);
    }

    /**
     * Starts the simulation page with the selected configuration.
     *
     * @param config the configuration values chosen on the main page
     */
    public void startSimulation(SimulationConfig config) {
        simulationTimeLimit = config.simulationTime();
        controller.startSimulation(config);
    }

    @FXML
    private void handleSlowDown() {
        controller.decreaseSpeed();
    }

    @FXML
    private void handleSpeedUp() {
        controller.increaseSpeed();
    }

    @FXML
    private void handlePause() {
        controller.togglePause();
    }

    /**
     * Changes the text on the pause button.
     *
     * @param text the new button text
     */
    public void setPauseButtonText(String text) {
        btnPause.setText(text);
    }

    /**
     * Updates the timer label on the simulation page.
     *
     * @param time the current simulation time from the engine
     */
    public void updateSimulationTime(double time) {
        double shownTime = Math.min(time, simulationTimeLimit);
        labelTimer.setText(String.format("%.2f", shownTime));
    }

    /**
     * Updates the four queue labels on the simulation page.
     *
     * @param queueLengths queue values for validation, market, limit, and execution
     */
    public void updateQueueLengths(int[] queueLengths) {
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
        controller.showOrderBook(snapshot);
    }

    /**
     * Opens the results page and sends final simulation data there.
     *
     * @param snapshot the final simulation snapshot
     * @param record the final database record with metrics
     */
    public void showResultsPage(StatisticsCollector.Snapshot snapshot, StatisticsAndMetricsRecord record) {
        controller.showResultsPage(snapshot, record);
    }

    /**
     * Replaces all visible rows in the order book table.
     *
     * @param rows rows already prepared by Controller
     */
    void setOrderBookRows(ObservableList<String[]> rows) {
        orderBookTable.setItems(rows);
    }

    /**
     * Returns the current JavaFX window of the simulation page.
     *
     * @return the stage that currently shows the simulation page
     */
    Stage getStage() {
        return (Stage) graph.getScene().getWindow();
    }
}

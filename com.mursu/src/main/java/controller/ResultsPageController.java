package controller;

import eds.database.Records.StatisticsAndMetricsRecord;
import eds.model.StatisticsCollector;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * JavaFX controller for the results page.
 *
 * Responsibilities:
 * - receive the final simulation data for results_view.fxml
 * - forward navigation actions back to {@link Controller}
 * - provide UI methods for writing formatted values into labels
 */
public class ResultsPageController {

    // These fields are linked to labels and buttons from results_view.fxml.
    @FXML private Label labelTotalArrived;
    @FXML private Label labelTotalExecuted;
    @FXML private Label labelRemaining;
    @FXML private Label labelAvgPrice;
    @FXML private Label labelAvgSpread;
    @FXML private Label labelThroughput;
    @FXML private Label labelAvgLatency;
    @FXML private Label labelFillRate;
    @FXML private Label labelAvgUtilization;
    @FXML private Label labelBottleNeck;
    @FXML private Label labelResults;
    @FXML private Button btnMain;

    private IViewToModelController controller;

    /**
     * Initializes the results page after the FXML file is loaded.
     * Creates the controller that fills the page and handles navigation.
     */
    @FXML
    private void initialize() {
        controller = new Controller(this);
    }

    /**
     * Fills the results page with final simulation values.
     *
     * @param snapshot the final simulation snapshot with aggregated metrics
     * @param record the database record used for human-readable insight text
     */
    public void setResults(StatisticsCollector.Snapshot snapshot, StatisticsAndMetricsRecord record) {
        controller.populateResults(snapshot, record);
    }

    @FXML
    private void handleBackToMain() {
        controller.openMainPageFromResults();
    }

    @FXML
    private void handleDownloadCsv() {
        // TODO
        // The Download CSV button calls this method.
    }

    Stage getStage() {
        return (Stage) btnMain.getScene().getWindow();
    }

    void setTotalArrivedText(String value) {
        labelTotalArrived.setText(value);
    }

    void setTotalExecutedText(String value) {
        labelTotalExecuted.setText(value);
    }

    void setRemainingText(String value) {
        labelRemaining.setText(value);
    }

    void setAveragePriceText(String value) {
        labelAvgPrice.setText(value);
    }

    void setAverageSpreadText(String value) {
        labelAvgSpread.setText(value);
    }

    void setThroughputText(String value) {
        labelThroughput.setText(value);
    }

    void setAverageLatencyText(String value) {
        labelAvgLatency.setText(value);
    }

    void setFillRateText(String value) {
        labelFillRate.setText(value);
    }

    void setAverageUtilizationText(String value) {
        labelAvgUtilization.setText(value);
    }

    void setBottleneckText(String value) {
        labelBottleNeck.setText(value);
    }

    void setResultsText(String value) {
        labelResults.setText(value);
    }
}

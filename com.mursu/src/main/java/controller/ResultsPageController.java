package controller;

import eds.database.PerformanceDescriber;
import eds.database.Records.StatisticsAndMetricsRecord;
import eds.model.StatisticsCollector;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controls the results page of the application.
 * This class shows final simulation metrics, bottleneck text,
 * and readable insight text after the simulation ends.
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

    /**
     * Fills the results page with final simulation values.
     *
     * @param snapshot the final simulation snapshot with aggregated metrics
     * @param record the database record used for human-readable insight text
     */
    public void setResults(StatisticsCollector.Snapshot snapshot, StatisticsAndMetricsRecord record) {
        // SimulationPageController.showResultsPage() calls this method.
        // It fills the labels on the results page with the final simulation data.
        if (snapshot == null) {
            return;
        }

        labelTotalArrived.setText(String.valueOf(snapshot.totalArrivedOrders()));
        labelTotalExecuted.setText(String.valueOf(snapshot.totalExecutedOrders()));
        labelRemaining.setText(String.valueOf(snapshot.remainingOrdersInBook()));
        labelAvgPrice.setText(format(snapshot.averageMidPrice()));
        labelAvgSpread.setText(format(snapshot.averageSpread()));
        labelThroughput.setText(format(snapshot.throughput()));
        labelAvgLatency.setText(format(snapshot.averageWaitingTime()));
        labelFillRate.setText(formatPercent(snapshot.fillRate()));
        labelAvgUtilization.setText(formatPercent(snapshot.averageServicePointUtilization()));

        // This converts the bottleneck index from the snapshot
        // into a human-readable queue name for the UI.
        if (snapshot.bottleneckServicePointIndex() == 0) {
            labelBottleNeck.setText("Validation queue");
        } else if (snapshot.bottleneckServicePointIndex() == 1) {
            labelBottleNeck.setText("Market matching queue");
        } else if (snapshot.bottleneckServicePointIndex() == 2) {
            labelBottleNeck.setText("Limit matching queue");
        } else if (snapshot.bottleneckServicePointIndex() == 3) {
            labelBottleNeck.setText("Execution queue");
        } else {
            labelBottleNeck.setText("No bottleneck");
        }

        // This builds the human-readable summary and insight text.
        labelResults.setText(buildPerformanceText(record));
    }

    @FXML
    private void handleBackToMain() {
        // The Back button in results_view.fxml calls this method.
        // It loads main_view.fxml and replaces the current results page with it.
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnMain.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open main page", e);
        }
    }

    @FXML
    private void handleDownloadCsv() {
        // The Download CSV button calls this method.
        // CSV export is still not implemented here.
    }

    /**
     * Formats a normal metric value with four decimal places.
     *
     * @param value the numeric value to format
     * @return formatted text with four decimal places
     */
    private String format(double value) {
        // setResults() uses this helper for normal metric values.
        return String.format("%.4f", value);
    }

    /**
     * Formats a ratio as percentage text.
     *
     * @param value the ratio value, for example 0.83
     * @return formatted percentage text, for example 83.0%
     */
    private String formatPercent(double value) {
        // setResults() uses this helper for percentage values.
        return String.format("%.1f%%", value * 100);
    }

    /**
     * Builds one readable text block from the saved statistics record.
     *
     * @param record the record that contains metrics for PerformanceDescriber
     * @return readable summary and insight text for the results page
     */
    private String buildPerformanceText(StatisticsAndMetricsRecord record) {
        // setResults() uses this helper to build one block of readable text.
        // PerformanceDescriber turns raw metric values into summary sentences.
        if (record == null) {
            return "";
        }

        PerformanceDescriber describer = new PerformanceDescriber(record);
        StringBuilder text = new StringBuilder(describer.describe());

        for (String insight : describer.generateInsights()) {
            text.append("\n- ").append(insight);
        }

        return text.toString();
    }
}

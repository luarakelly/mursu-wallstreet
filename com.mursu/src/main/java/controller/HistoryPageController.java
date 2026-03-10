package controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * JavaFX controller for the history page.
 *
 * Responsibilities:
 * - receive user actions from history_view.fxml
 * - forward navigation actions back to {@link Controller}
 */
public class HistoryPageController {
    // These fields are linked to the results table from history_view.fxml.
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

    // Not used but saved jsut in case
    // @FXML private TextField inputSearch;
    // @FXML private Button btnSearch;

    // This field is linked to the select with history records about simulation runs.
    @FXML private ComboBox<String> menuHistoryList;

    // Back to main page button
    @FXML private Button btnMain;

    private IViewToModelController controller;

    @FXML
    private void initialize() {
        controller = new Controller(this);
        controller.initializeHistoryPage();
    }

    // Starts main page back
    @FXML
    private void handleBackToMain() {
        controller.openMainPageFromHistory();
    }

    @FXML
    private void handleHistorySelection() {
        int selectedIndex = menuHistoryList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            controller.updateHistoryPage(selectedIndex);
        }
    }

    void setHistoryOptions(ObservableList<String> items, int selectedIndex) {
        menuHistoryList.setItems(items);
        if (selectedIndex < 0 || selectedIndex >= items.size()) {
            menuHistoryList.getSelectionModel().clearSelection();
            return;
        }
        menuHistoryList.getSelectionModel().select(selectedIndex);
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

//    String getSearchInput() {
//        return inputSearch.getText();
//    }

    Stage getStage() {
        return (Stage) btnMain.getScene().getWindow();
    }
}

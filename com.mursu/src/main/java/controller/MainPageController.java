package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * JavaFX controller for the main input page.
 *
 * Responsibilities:
 * - receive user actions from main_view.fxml
 * - forward business actions to {@link Controller}
 * - expose getter and setter methods for the page fields
 */
public class MainPageController {
    // These fields are linked to input elements from main_view.fxml.
    @FXML private TextField inputTitle;
    @FXML private TextField inputSpeed;
    @FXML private TextField inputDuration;
    @FXML private TextField inputSeed;
    @FXML private TextField inputTick;
    @FXML private TextField inputVolatility;
    @FXML private TextField inputInitialPrice;
    @FXML private TextField inputMeanArr;
    @FXML private TextField inputMeanValid;
    @FXML private TextField inputMarketMatch;
    @FXML private TextField inputLimitMatch;
    @FXML private TextField inputExecution;
    @FXML private TextField inputMarketRatio;
    @FXML private TextField inputBuyRatio;
    @FXML private Button btnStart;

    private IViewToModelController controller;

    @FXML
    private void initialize() {
        controller = new Controller(this);
        controller.initializeMainPage();
    }

    @FXML
    private void handleStartSimulation() {
        controller.openSimulationPageFromMain();
    }

    @FXML
    private void applyBalancedPreset() {
        // The Balanced preset button forwards here.
        controller.applyBalancedPreset();
    }

    @FXML
    private void applySlowPreset() {
        // The Slow preset button forwards here.
        controller.applySlowPreset();
    }

    @FXML
    private void applyHighFrequencyPreset() {
        // The High frequency preset button forwards here.
        controller.applyHighFrequencyPreset();
    }

    @FXML
    private void applyVolatilePreset() {
        // The Volatile preset button forwards here.
        controller.applyVolatilePreset();
    }

    Stage getStage() {
        return (Stage) btnStart.getScene().getWindow();
    }

    String getTitleInput() {
        return inputTitle.getText();
    }

    String getSpeedInput() {
        return inputSpeed.getText();
    }

    String getDurationInput() {
        return inputDuration.getText();
    }

    String getSeedInput() {
        return inputSeed.getText();
    }

    String getTickInput() {
        return inputTick.getText();
    }

    String getVolatilityInput() {
        return inputVolatility.getText();
    }

    String getInitialPriceInput() {
        return inputInitialPrice.getText();
    }

    String getMeanArrivalInput() {
        return inputMeanArr.getText();
    }

    String getMeanValidationInput() {
        return inputMeanValid.getText();
    }

    String getMarketMatchInput() {
        return inputMarketMatch.getText();
    }

    String getLimitMatchInput() {
        return inputLimitMatch.getText();
    }

    String getExecutionInput() {
        return inputExecution.getText();
    }

    String getMarketRatioInput() {
        return inputMarketRatio.getText();
    }

    String getBuyRatioInput() {
        return inputBuyRatio.getText();
    }

    // Setter helpers let Controller apply presets without touching FXML fields directly.
    void setTitleInput(String value) {
        inputTitle.setText(value);
    }

    void setSpeedInput(String value) {
        inputSpeed.setText(value);
    }

    void setDurationInput(String value) {
        inputDuration.setText(value);
    }

    void setSeedInput(String value) {
        inputSeed.setText(value);
    }

    void setTickInput(String value) {
        inputTick.setText(value);
    }

    void setVolatilityInput(String value) {
        inputVolatility.setText(value);
    }

    void setInitialPriceInput(String value) {
        inputInitialPrice.setText(value);
    }

    void setMeanArrivalInput(String value) {
        inputMeanArr.setText(value);
    }

    void setMeanValidationInput(String value) {
        inputMeanValid.setText(value);
    }

    void setMarketMatchInput(String value) {
        inputMarketMatch.setText(value);
    }

    void setLimitMatchInput(String value) {
        inputLimitMatch.setText(value);
    }

    void setExecutionInput(String value) {
        inputExecution.setText(value);
    }

    void setMarketRatioInput(String value) {
        inputMarketRatio.setText(value);
    }

    void setBuyRatioInput(String value) {
        inputBuyRatio.setText(value);
    }
}

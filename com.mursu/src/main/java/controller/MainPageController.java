package controller;

import eds.config.SimulationConfig;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controls the main page of the application.
 * This class reads values from the input fields,
 * applies preset values, and opens the (next) simulation page.
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

    @FXML
    private void initialize() {
        // FXMLLoader in SimulatorGUI.java loads main_view.fxml,
        // creates MainPageController, and then calls this method.
        // This is used to put the first preset values into the main page fields.
        applyConfigToInputs(SimulationConfig.balanced());
    }

    @FXML
    // The Start button in main_view.fxml calls this method.
    // It reads the values from the main page and opens the simulation page.
    private void handleStartSimulation() {
        // This method from the same class reads all current text field values
        // and builds one SimulationConfig object for the next page.
        SimulationConfig config = readConfigFromInputs();

        try {
            // FXMLLoader reads simulation_view.fxml and builds that page.
            // loader.load() also creates SimulationPageController,
            // because that controller is written in simulation_view.fxml.
            // loader.getController() returns that created page controller.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/simulation_view.fxml"));
            Parent root = loader.load();
            SimulationPageController controller = loader.getController();

            // This sends the selected values from the main page to the simulation page.
            controller.startSimulation(config);

            // This takes the current window from the Start button
            // and replaces the main page with the simulation page.
            Stage stage = (Stage) btnStart.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open simulation page", e);
        }
    }

    @FXML
    private void applyBalancedPreset() {
        // The Balanced preset button in main_view.fxml calls this method.
        // It writes the balanced preset values into the input fields.
        applyConfigToInputs(SimulationConfig.balanced());
    }

    @FXML
    private void applySlowPreset() {
        // The Slow preset button in main_view.fxml calls this method.
        // It writes the slow stable preset values into the input fields.
        applyConfigToInputs(SimulationConfig.slowStable());
    }

    @FXML
    private void applyHighFrequencyPreset() {
        // The High frequency preset button in main_view.fxml calls this method.
        // It writes those preset values into the input fields.
        applyConfigToInputs(SimulationConfig.highFrequency());
    }

    @FXML
    private void applyVolatilePreset() {
        // The Volatile preset button in main_view.fxml calls this method.
        // It writes the volatile market values into the input fields.
        applyConfigToInputs(SimulationConfig.volatileMarket());
    }

    /**
     * Copies all values from one simulation config object into the main page fields.
     *
     * @param config the config object whose values are shown in the input fields
     */
    private void applyConfigToInputs(SimulationConfig config) {
        // Preset methods and initialize() use this helper.
        // It copies values from one config object into all text fields on the page.
        inputTitle.setText(config.title());
        inputSpeed.setText(String.valueOf(config.delay()));
        inputDuration.setText(String.valueOf(config.simulationTime()));
        inputSeed.setText(String.valueOf(config.seed()));
        inputTick.setText(String.valueOf(config.tickSize()));
        inputVolatility.setText(String.valueOf(config.priceVolatility()));
        inputInitialPrice.setText(String.valueOf(config.initialMidPrice()));
        inputMeanArr.setText(String.valueOf(config.arrivalMean()));
        inputMeanValid.setText(String.valueOf(config.meanValidation()));
        inputMarketMatch.setText(String.valueOf(config.meanMarketMatching()));
        inputLimitMatch.setText(String.valueOf(config.meanLimitMatching()));
        inputExecution.setText(String.valueOf(config.meanExecution()));
        inputMarketRatio.setText(String.valueOf(config.marketOrderRatio()));
        inputBuyRatio.setText(String.valueOf(config.buyOrderRatio()));
    }

    /**
     * Reads values from the main page fields and builds one simulation config object.
     *
     * @return a SimulationConfig built from the current input field values
     */
    private SimulationConfig readConfigFromInputs() {
        // handleStartSimulation() uses this method before opening the next page.
        // It reads values from the text fields and creates one SimulationConfig object.
        return new SimulationConfig(
                inputTitle.getText(),
                Long.parseLong(inputSeed.getText()),
                Double.parseDouble(inputMeanValid.getText()),
                Double.parseDouble(inputMarketMatch.getText()),
                Double.parseDouble(inputLimitMatch.getText()),
                Double.parseDouble(inputExecution.getText()),
                Double.parseDouble(inputMeanArr.getText()),
                Double.parseDouble(inputMarketRatio.getText()),
                Double.parseDouble(inputBuyRatio.getText()),
                Double.parseDouble(inputInitialPrice.getText()),
                Double.parseDouble(inputVolatility.getText()),
                Double.parseDouble(inputTick.getText()),
                Double.parseDouble(inputDuration.getText()),
                Long.parseLong(inputSpeed.getText())
        );
    }
}

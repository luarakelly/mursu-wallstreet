package controller;

import eds.database.IQueries;
import eds.database.PerformanceDescriber;
import eds.database.Queries;
import eds.database.Records.StatisticsAndMetricsRecord;
import eds.framework.Clock;
import eds.config.SimulationConfig;
import eds.model.IEngine;
import eds.model.MyEngine;
import eds.model.OrderBook;
import eds.model.StatisticsCollector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

/**
 * Controls the simulation logic for the simulation page.
 * This class creates the engine, starts the run, changes speed,
 * pauses and resumes the run, and sends engine updates to the UI.
 */
public class Controller implements IViewToModelController, IModelToViewController {
    private static final DateTimeFormatter HISTORY_DATE_INPUT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter HISTORY_DATE_OUTPUT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Pages from the UI.
    private final MainPageController mainPageController;
    private final SimulationPageController simulationPageController;
    private final ResultsPageController resultsPageController;
    private final HistoryPageController historyPageController;

    // startSimulation() creates MyEngine and stores it here.
    private IEngine engine;

    // togglePause() uses these fields to remember pause state and old delay.
    private boolean isPaused = false;
    private long delayBeforePause;
    private List<StatisticsAndMetricsRecord> historyRecords = Collections.emptyList();

    /**
     * Creates a controller for one simulation page.
     *
     * @param simulationPageController the simulation page that receives UI updates
     */
    public Controller(SimulationPageController simulationPageController) {
        // Uses in SimulationPageController.initialize().
        this.mainPageController = null;
        this.simulationPageController = simulationPageController;
        this.resultsPageController = null;
        this.historyPageController = null;
    }

    /**
     * Creates a controller for the main page.
     *
     * @param mainPageController the main page that sends user input to this controller
     */
    public Controller(MainPageController mainPageController) {
        // Uses in MainPageController.initialize().
        this.simulationPageController = null;
        this.mainPageController = mainPageController;
        this.resultsPageController = null;
        this.historyPageController = null;
    }

    /**
     * Creates a controller for the results page.
     *
     * @param resultsPageController the results page that shows final simulation output
     */
    public Controller(ResultsPageController resultsPageController) {
        // Uses in ResultsPageController.initialize().
        this.mainPageController = null;
        this.simulationPageController = null;
        this.resultsPageController = resultsPageController;
        this.historyPageController = null;
    }

    /**
     * Creates a controller for the history page.
     *
     * @param historyPageController the history page that handles navigation
     */
    public Controller(HistoryPageController historyPageController) {
        // Uses in HistoryPageController.initialize().
        this.mainPageController = null;
        this.simulationPageController = null;
        this.resultsPageController = null;
        this.historyPageController = historyPageController;
    }

    /**
     * Applies the default preset values on the main page inputs.
     * The balanced preset chose as default, but can be changed.
     */
    @Override
    public void initializeMainPage() {
        applyConfigToMainInputs(SimulationConfig.balanced());
    }

    /**
     * Loads saved runs from the database and shows them in the history page menu.
     */
    @Override
    public void initializeHistoryPage() {

        // Get records from database
        IQueries queries = new Queries();
        historyRecords = queries.findAll();

        ObservableList<String> items = FXCollections.observableArrayList();

        // Prepare items for the dropdown
        for (StatisticsAndMetricsRecord record : historyRecords) {

            String runName = record.runName();
            String timestamp = record.runTimestamp();
            String formattedTimestamp;

            try {
                LocalDateTime date = LocalDateTime.parse(timestamp, HISTORY_DATE_INPUT);
                formattedTimestamp = date.format(HISTORY_DATE_OUTPUT);
            } catch (DateTimeParseException e) {
                formattedTimestamp = timestamp.replace("T", " ");
            }

            items.add(runName + " " + formattedTimestamp);
        }

        // Fill dropdown
        if (historyRecords.isEmpty()) {
            historyPageController.setHistoryOptions(items, -1);
        } else {
            historyPageController.setHistoryOptions(items, 0);
        }

        // Show data
        if (!historyRecords.isEmpty()) {
            updateHistoryPage(0);
        } else {
            historyPageController.setTotalArrivedText("-");
            historyPageController.setTotalExecutedText("-");
            historyPageController.setRemainingText("-");
            historyPageController.setAveragePriceText("-");
            historyPageController.setAverageSpreadText("-");
            historyPageController.setThroughputText("-");
            historyPageController.setAverageLatencyText("-");
            historyPageController.setFillRateText("-");
            historyPageController.setAverageUtilizationText("-");
            historyPageController.setBottleneckText("No bottleneck");
            historyPageController.setResultsText("No saved simulations found.");
        }
    }

    /**
     * Applies the balanced preset to the main page inputs.
     */
    @Override
    public void applyBalancedPreset() {
        applyConfigToMainInputs(SimulationConfig.balanced());
    }

    /**
     * Applies the slow stable preset to the main page inputs.
     */
    @Override
    public void applySlowPreset() {
        applyConfigToMainInputs(SimulationConfig.slowStable());
    }

    /**
     * Applies the high-frequency preset to the main page inputs.
     */
    @Override
    public void applyHighFrequencyPreset() {
        // MainPageController.applyHighFrequencyPreset() forwards here.
        applyConfigToMainInputs(SimulationConfig.highFrequency());
    }

    /**
     * Applies the volatile market preset to the main page inputs.
     */
    @Override
    public void applyVolatilePreset() {
        // MainPageController.applyVolatilePreset() forwards here.
        applyConfigToMainInputs(SimulationConfig.volatileMarket());
    }

    /**
     * Reads main page inputs, and, by user click on the button, opens the simulation page and starts the run there.
     */
    @Override
    public void openSimulationPageFromMain() {
        SimulationConfig config = readConfigFromMainInputs();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/simulation_view.fxml"));
            Parent root = loader.load();
            SimulationPageController page = loader.getController();
            page.startSimulation(config);

            Stage stage = mainPageController.getStage();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open simulation page", e);
        }
    }

    /**
     * Opens the history page from the main page after user clicks the History button.
     */
    @Override
    public void openHistoryPageFromMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/history_view.fxml"));
            Parent root = loader.load();

            Stage stage = mainPageController.getStage();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open history page", e);
        }
    }

    /**
     * Fills the results page labels with already computed simulation output.
     *
     * @param snapshot the final simulation snapshot
     * @param record the saved metrics record used for readable insight text
     */
    @Override
    public void updateResultsPage(StatisticsCollector.Snapshot snapshot, StatisticsAndMetricsRecord record) {
        double averageUtilization = (record.utilizationValidation()
                + record.utilizationMarket()
                + record.utilizationLimit()
                + record.utilizationExecution()) / 4.0;
        int bottleneckIndex = 0;
        double maxQueue = record.avgQueueValidation();

        if (record.avgQueueMarket() > maxQueue) {
            maxQueue = record.avgQueueMarket();
            bottleneckIndex = 1;
        }
        if (record.avgQueueLimit() > maxQueue) {
            maxQueue = record.avgQueueLimit();
            bottleneckIndex = 2;
        }
        if (record.avgQueueExecution() > maxQueue) {
            maxQueue = record.avgQueueExecution();
            bottleneckIndex = 3;
        }

        String bottleneckText = maxQueue > 0
                ? getBottleneckText(bottleneckIndex)
                : "No bottleneck";

        resultsPageController.setTotalArrivedText(String.valueOf(record.totalOrders()));
        resultsPageController.setTotalExecutedText(String.valueOf(record.filledOrders()));
        resultsPageController.setRemainingText(String.valueOf(record.remainingOrders()));
        resultsPageController.setAveragePriceText(format(record.avgMidPrice()));
        resultsPageController.setAverageSpreadText(format(record.avgSpread()));
        resultsPageController.setThroughputText(format(record.throughput()));
        resultsPageController.setAverageLatencyText(format(record.avgLatency()));
        resultsPageController.setFillRateText(formatPercent(record.fillRate()));
        resultsPageController.setAverageUtilizationText(formatPercent(averageUtilization));
        resultsPageController.setBottleneckText(bottleneckText);
        resultsPageController.setResultsText(buildPerformanceText(record));
    }

    /**
     * Fills the history page from one database record selected by the user.
     *
     * @param recordIndex index of the selected saved simulation run
     */
    @Override
    public void updateHistoryPage(int recordIndex) {
        StatisticsAndMetricsRecord record = historyRecords.get(recordIndex);
        double averageUtilization = (record.utilizationValidation()
                + record.utilizationMarket()
                + record.utilizationLimit()
                + record.utilizationExecution()) / 4.0;
        int bottleneckIndex = 0;
        double maxQueue = record.avgQueueValidation();

        if (record.avgQueueMarket() > maxQueue) {
            maxQueue = record.avgQueueMarket();
            bottleneckIndex = 1;
        }
        if (record.avgQueueLimit() > maxQueue) {
            maxQueue = record.avgQueueLimit();
            bottleneckIndex = 2;
        }
        if (record.avgQueueExecution() > maxQueue) {
            maxQueue = record.avgQueueExecution();
            bottleneckIndex = 3;
        }

        String bottleneckText = maxQueue > 0
                ? getBottleneckText(bottleneckIndex)
                : "No bottleneck";

        historyPageController.setTotalArrivedText(String.valueOf(record.totalOrders()));
        historyPageController.setTotalExecutedText(String.valueOf(record.filledOrders()));
        historyPageController.setRemainingText(String.valueOf(record.remainingOrders()));
        historyPageController.setAveragePriceText(format(record.avgMidPrice()));
        historyPageController.setAverageSpreadText(format(record.avgSpread()));
        historyPageController.setThroughputText(format(record.throughput()));
        historyPageController.setAverageLatencyText(format(record.avgLatency()));
        historyPageController.setFillRateText(formatPercent(record.fillRate()));
        historyPageController.setAverageUtilizationText(formatPercent(averageUtilization));
        historyPageController.setBottleneckText(bottleneckText);
        historyPageController.setResultsText(buildPerformanceText(record));
    }

    /**
     * Opens the main page from the results page after user clicks on the button to return to the main page.
     */
    @Override
    public void openMainPageFromResults() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_view.fxml"));
            Parent root = loader.load();
            Stage stage = resultsPageController.getStage();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open main page", e);
        }
    }

    /**
     * Opens the main page from the history page after user clicks return.
     */
    @Override
    public void openMainPageFromHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_view.fxml"));
            Parent root = loader.load();
            Stage stage = historyPageController.getStage();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open main page", e);
        }
    }

    /**
     * Starts a new simulation run with the selected configuration.
     *
     * @param config the full set of values for the simulation run
     */
    @Override
    public void startSimulation(SimulationConfig config) {
        engine = new MyEngine(
                this,
                config.seed(),
                config.meanValidation(),
                config.meanMarketMatching(),
                config.meanLimitMatching(),
                config.meanExecution(),
                config.arrivalMean(),
                config.marketOrderRatio(),
                config.buyOrderRatio(),
                config.initialMidPrice(),
                config.priceVolatility(),
                config.tickSize(),
                config.title()
        );

        // These values are copied from the page config into the engine before start.
        engine.setSimulationTime(config.simulationTime());
        engine.setDelay(config.delay());

        // MyEngine extends Thread, so the simulation starts here in a separate thread.
        engine.start();
    }

    /**
     * Makes the simulation slower by increasing the delay.
     */
    @Override
    public void decreaseSpeed() {
        // It increases delay on 20% so one simulation step waits longer.

        long newDelay = Math.max(0, Math.round(engine.getDelay() * 1.2));
        engine.setDelay(newDelay);

        // The engine may be sleeping on the old delay now.
        // interrupt() wakes it up so the new delay starts immediately.
        engine.interrupt();
    }

    /**
     * Makes the simulation faster by decreasing the delay.
     */
    @Override
    public void increaseSpeed() {
        // It decreases delay on 20% so one simulation step waits less.

        long newDelay = Math.max(0, Math.round(engine.getDelay() * 0.8));
        engine.setDelay(newDelay);

        // The engine may be sleeping on the old delay now.
        // interrupt() wakes it up so the new delay starts immediately.
        engine.interrupt();
    }

    /**
     * Pauses the simulation or resumes it if it is already paused.
     */
    @Override
    public void togglePause() {
        // SimulationPageController.handlePause() calls this method.
        // It changes engine delay and then changes the pause button text on the page.
        if (engine == null) {
            return;
        }

        isPaused = !isPaused;

        if (isPaused) {
            delayBeforePause = engine.getDelay();
            engine.setDelay(1000000); // ms
        } else {
            engine.setDelay(delayBeforePause);

            // The engine may still be sleeping on the pause delay.
            // interrupt() wakes it up so resume works right away.
            engine.interrupt();
        }

        simulationPageController.setPauseButtonText(isPaused ? "Resume" : "Pause");
    }

    /**
     * Opens the results page after the engine finishes the simulation.
     *
     * @param time the final simulation time reported by the engine
     */
    @Override
    public void showEndTime(double time) {
        // Read the final snapshot and database record from MyEngine.
        final StatisticsCollector.Snapshot snapshot = engine.getStatisticsSnapshot();
        final StatisticsAndMetricsRecord record = engine.getStatisticsRecord();

        // MyEngine runs in its own thread, not in the JavaFX UI thread.
        // UI pages and controls can be changed only from the JavaFX UI thread.
        // Platform.runLater() moves this page change to the correct thread.
        Platform.runLater(() -> openResultsPage(snapshot, record));
    }

    /**
     * Updates the timer and queue labels on the simulation page.
     */
    @Override
    public void updateTimeAndQueues() {
        // Read queue data from the engine and update timer and queue labels on each tick.
        final int[] queueLengths = engine == null ? null : engine.getQueueLengths();

        // The engine calculates this in its own thread.
        // Platform.runLater() moves the label updates to the JavaFX UI thread.
        Platform.runLater(() -> {
            simulationPageController.updateSimulationTime(Clock.getInstance().getTime());
            if (queueLengths != null) {
                simulationPageController.updateQueueLengths(queueLengths);
            }
        });
    }

    /**
     * Sends a new order book snapshot to the simulation page table.
     *
     * @param snapshot the latest order book state from the engine
     */
    @Override
    public void updateOrderBook(OrderBook.OrderBookSnapshot snapshot) {
        // Send the newest order book snapshot to the table on the simulation page after each tick.
        final ObservableList<String[]> rows = buildOrderBookRows(snapshot);

        // The table is a JavaFX UI element,
        // so this update must run in the JavaFX UI thread.
        Platform.runLater(() -> simulationPageController.setOrderBookRows(rows));
    }

    /**
     * Opens the results page and sends final simulation data there.
     *
     * @param snapshot the final simulation snapshot
     * @param record the final database record with metrics
     */
    @Override
    public void showResultsPage(StatisticsCollector.Snapshot snapshot, StatisticsAndMetricsRecord record) {
        // SimulationPageController.showResultsPage() forwards here.
        openResultsPage(snapshot, record);
    }

    /**
     * Requests a fresh order book table update for the simulation page.
     *
     * @param snapshot the latest order book snapshot
     */
    @Override
    public void showOrderBook(OrderBook.OrderBookSnapshot snapshot) {
        updateOrderBook(snapshot);
    }

    /**
     * Copies one simulation config into all main page text fields.
     *
     * @param config the config whose values will be shown on the main page
     */
    private void applyConfigToMainInputs(SimulationConfig config) {
        mainPageController.setTitleInput(config.title());
        mainPageController.setSpeedInput(String.valueOf(config.delay()));
        mainPageController.setDurationInput(String.valueOf(config.simulationTime()));
        mainPageController.setSeedInput(String.valueOf(config.seed()));
        mainPageController.setTickInput(String.valueOf(config.tickSize()));
        mainPageController.setVolatilityInput(String.valueOf(config.priceVolatility()));
        mainPageController.setInitialPriceInput(String.valueOf(config.initialMidPrice()));
        mainPageController.setMeanArrivalInput(String.valueOf(config.arrivalMean()));
        mainPageController.setMeanValidationInput(String.valueOf(config.meanValidation()));
        mainPageController.setMarketMatchInput(String.valueOf(config.meanMarketMatching()));
        mainPageController.setLimitMatchInput(String.valueOf(config.meanLimitMatching()));
        mainPageController.setExecutionInput(String.valueOf(config.meanExecution()));
        mainPageController.setMarketRatioInput(String.valueOf(config.marketOrderRatio()));
        mainPageController.setBuyRatioInput(String.valueOf(config.buyOrderRatio()));
    }

    /**
     * Reads all current main page field values into the SimulationConfig.
     *
     * @return a SimulationConfig built from the current main page inputs
     */
    private SimulationConfig readConfigFromMainInputs() {
        // openSimulationPageFromMain() uses this helper before opening simulation_view.fxml.
        return new SimulationConfig(
                mainPageController.getTitleInput(),
                Long.parseLong(mainPageController.getSeedInput()),
                Double.parseDouble(mainPageController.getMeanValidationInput()),
                Double.parseDouble(mainPageController.getMarketMatchInput()),
                Double.parseDouble(mainPageController.getLimitMatchInput()),
                Double.parseDouble(mainPageController.getExecutionInput()),
                Double.parseDouble(mainPageController.getMeanArrivalInput()),
                Double.parseDouble(mainPageController.getMarketRatioInput()),
                Double.parseDouble(mainPageController.getBuyRatioInput()),
                Double.parseDouble(mainPageController.getInitialPriceInput()),
                Double.parseDouble(mainPageController.getVolatilityInput()),
                Double.parseDouble(mainPageController.getTickInput()),
                Double.parseDouble(mainPageController.getDurationInput()),
                Long.parseLong(mainPageController.getSpeedInput())
        );
    }

    /**
     * Converts one order book snapshot into table rows for the simulation page.
     *
     * @param snapshot the current order book snapshot from the engine
     * @return table rows ready for orderBookTable
     */
    private ObservableList<String[]> buildOrderBookRows(OrderBook.OrderBookSnapshot snapshot) {
        // ObservableList is a JavaFX list that the UI can watch for changes. Here it helps to update the table with bids and asks.
        ObservableList<String[]> rows = FXCollections.observableArrayList();
        if (snapshot == null) {
            return rows;
        }

        int rowCount = Math.max(snapshot.asks().size(), snapshot.bids().size());
        String spreadText = snapshot.spread().isPresent()
                ? String.format("%.4f", snapshot.spread().getAsDouble())
                : "";

        for (int i = 0; i < rowCount; i++) {
            String askShare = "";
            String askPrice = "";
            String bidShare = "";
            String bidPrice = "";
            String rowSpread = i == 0 ? spreadText : "";

            if (i < snapshot.asks().size()) {
                OrderBook.PriceLevel ask = snapshot.asks().get(i);
                askShare = String.valueOf(ask.totalQty());
                askPrice = String.format("%.4f", ask.price());
            }

            if (i < snapshot.bids().size()) {
                OrderBook.PriceLevel bid = snapshot.bids().get(i);
                bidShare = String.valueOf(bid.totalQty());
                bidPrice = String.format("%.4f", bid.price());
            }

            rows.add(new String[] { askShare, askPrice, bidShare, bidPrice, rowSpread });
        }

        return rows;
    }

    /**
     * Loads the results page and sends final simulation data there.
     *
     * @param snapshot the final simulation snapshot
     * @param record the final database record with metrics
     */
    private void openResultsPage(StatisticsCollector.Snapshot snapshot, StatisticsAndMetricsRecord record) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/results_view.fxml"));
            Parent root = loader.load();
            ResultsPageController page = loader.getController();
            page.updateResultsPage(snapshot, record);

            Stage stage = simulationPageController.getStage();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open results page", e);
        }
    }

    /**
     * Maps the bottleneck queue index to the text shown in the table on the results page.
     *
     * @param index the bottleneck service point index from the snapshot
     * @return human-readable queue name
     */
    private String getBottleneckText(int index) {
        if (index == 0) {
            return "Validation queue";
        }
        if (index == 1) {
            return "Market matching queue";
        }
        if (index == 2) {
            return "Limit matching queue";
        }
        if (index == 3) {
            return "Execution queue";
        }
        return "No bottleneck";
    }

    /**
     * Formats a normal numeric metric with four decimal places.
     *
     * @param value the metric value to format
     * @return formatted text with four decimal places
     */
    private String format(double value) {
        return String.format("%.4f", value);
    }

    /**
     * Formats a ratio as percentage text.
     *
     * @param value the ratio value, for example 0.83
     * @return formatted percentage text, for example 83.0%
     */
    private String formatPercent(double value) {
        return String.format("%.1f%%", value * 100);
    }

    /**
     * Builds one readable result text block from the saved statistics record.
     *
     * @param record the record that contains metrics for PerformanceDescriber
     * @return readable summary and insight text for the results page
     */
    private String buildPerformanceText(StatisticsAndMetricsRecord record) {
        PerformanceDescriber describer = new PerformanceDescriber(record);
        StringBuilder text = new StringBuilder(describer.describe());

        for (String insight : describer.generateInsights()) {
            text.append("\n- ").append(insight);
        }

        return text.toString();
    }
}

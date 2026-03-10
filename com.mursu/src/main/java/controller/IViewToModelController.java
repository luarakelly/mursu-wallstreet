package controller;

import eds.config.SimulationConfig;
import eds.database.Records.StatisticsAndMetricsRecord;
import eds.model.OrderBook;
import eds.model.StatisticsCollector;

/**
 * Common controller operations that all page files can call.
 */
public interface IViewToModelController {
    void initializeMainPage();

    void initializeHistoryPage();

    void applyBalancedPreset();

    void applySlowPreset();

    void applyHighFrequencyPreset();

    void applyVolatilePreset();

    void openHistoryPageFromMain();

    void openSimulationPageFromMain();

    void startSimulation(SimulationConfig config);

    void decreaseSpeed();

    void increaseSpeed();

    void togglePause();

    void showOrderBook(OrderBook.OrderBookSnapshot snapshot);

    void showResultsPage(StatisticsCollector.Snapshot snapshot, StatisticsAndMetricsRecord record);

    void updateResultsPage(StatisticsCollector.Snapshot snapshot, StatisticsAndMetricsRecord record);

    void updateHistoryPage(int recordIndex);

    void openMainPageFromResults();

    void openMainPageFromHistory();
}

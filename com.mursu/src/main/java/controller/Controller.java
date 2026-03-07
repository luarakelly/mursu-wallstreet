package controller;

import eds.database.Records.StatisticsAndMetricsRecord;
import eds.framework.Clock;
import eds.config.SimulationConfig;
import eds.model.MyEngine;
import eds.model.OrderBook;
import eds.model.StatisticsCollector;
import javafx.application.Platform;

/**
 * Controls the simulation logic for the simulation page.
 * This class creates the engine, starts the run, changes speed,
 * pauses and resumes the run, and sends engine updates to the UI.
 */
public class Controller {
    // SimulationPageController creates this class and receives UI updates from it.
    private final SimulationPageController simulationPageController;

    // startSimulation(...) creates MyEngine and stores it here.
    private MyEngine engine;

    // togglePause() uses these fields to remember pause state and old delay.
    private boolean isPaused = false;
    private long delayBeforePause;

    /**
     * Creates a controller for one simulation page.
     *
     * @param simulationPageController the simulation page that receives UI updates
     */
    public Controller(SimulationPageController simulationPageController) {
        // SimulationPageController.initialize() creates this object.
        // This reference is stored so this class can update that page later.
        this.simulationPageController = simulationPageController;
    }

    /**
     * Starts a new simulation run with the selected configuration.
     *
     * @param config the full set of values for the simulation run
     */
    public void startSimulation(SimulationConfig config) {
        // SimulationPageController.startSimulation() calls this method.
        // This is the place where MyEngine is created for the selected run.
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
        ((Thread) engine).start();
    }

    /**
     * Makes the simulation slower by increasing the delay.
     */
    public void decreaseSpeed() {
        // SimulationPageController.handleSlowDown() calls this method.
        // It increases delay so one simulation step waits longer.
        if (engine == null) {
            return;
        }

        long newDelay = Math.max(0, Math.round(engine.getDelay() * 1.2));
        engine.setDelay(newDelay);

        // The engine may be sleeping on the old delay now.
        // interrupt() wakes it up so the new delay starts immediately.
        ((Thread) engine).interrupt();
    }

    /**
     * Makes the simulation faster by decreasing the delay.
     */
    public void increaseSpeed() {
        // SimulationPageController.handleSpeedUp() calls this method.
        // It decreases delay so one simulation step waits less.
        if (engine == null) {
            return;
        }

        long newDelay = Math.max(0, Math.round(engine.getDelay() * 0.8));
        engine.setDelay(newDelay);

        // The engine may be sleeping on the old delay now.
        // interrupt() wakes it up so the new delay starts immediately.
        ((Thread) engine).interrupt();
    }

    /**
     * Pauses the simulation or resumes it if it is already paused.
     */
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
            ((Thread) engine).interrupt();
        }

        simulationPageController.setPauseButtonText(isPaused ? "Resume" : "Pause");
    }

    /**
     * Opens the results page after the engine finishes the simulation.
     *
     * @param time the final simulation time reported by the engine
     */
    public void showEndTime(double time) {
        // MyEngine.results() calls this method after the run ends.
        // The final snapshot and database record are read from MyEngine here.
        final StatisticsCollector.Snapshot snapshot = engine.getStatisticsSnapshot();
        final StatisticsAndMetricsRecord record = engine.getStatisticsRecord();

        // MyEngine runs in its own thread, not in the JavaFX UI thread.
        // UI pages and controls can be changed only from the JavaFX UI thread.
        // Platform.runLater(...) moves this page change to the correct thread.
        Platform.runLater(() -> simulationPageController.showResultsPage(snapshot, record));
    }

    /**
     * Updates the timer and queue labels on the simulation page.
     */
    public void updateTimeAndQueues() {
        // MyEngine.runEvent() calls this after a new ARRIVAL event.
        // It reads queue data from the engine and updates timer and queue labels.
        final int[] queueLengths = engine == null ? null : engine.getQueueLengths();

        // The engine calculates this in its own thread.
        // Platform.runLater(...) moves the label updates to the JavaFX UI thread.
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
    public void updateOrderBook(OrderBook.OrderBookSnapshot snapshot) {
        // MyEngine.afterCycle() calls this method after each simulation cycle.
        // It sends the newest order book snapshot to the table on the simulation page.

        // The table is a JavaFX UI element,
        // so this update must run in the JavaFX UI thread.
        Platform.runLater(() -> simulationPageController.showOrderBook(snapshot));
    }
}

package controller;

import eds.database.Records.StatisticsAndMetricsRecord;
import eds.framework.Clock;

import eds.config.SimulationConfig;

import eds.model.MyEngine;
import eds.model.OrderBook;
import eds.model.StatisticsCollector;
import javafx.application.Platform;

/**
 * Controls the simulation logic (mostly for the simulation page).
 * This class creates the engine, starts the run, changes speed,
 * pauses and resumes the run, and sends engine updates to the UI.
 */
public class Controller {
    // Used to send simulation updates to the simulation page.
    private final SimulationPageController simulationPageController;

    // Used to start the simulation and control its speed.
    private MyEngine engine;

    // Used for pause and resume the simulation.
    private boolean isPaused = false;
    private long delayBeforePause;

    /**
     * Creates the main controller for one simulation page.
     *
     * @param simulationPageController the simulation page that receives UI updates
     */
    public Controller(SimulationPageController simulationPageController) {
        // Save the simulation page controller so we can update the screen later.
        this.simulationPageController = simulationPageController;
    }

    /**
     * Starts a new simulation run with the selected configuration.
     *
     * @param config the full set of values for the simulation run
     */
    // SimulationPageController calls startSimulation() when the simulation page starts.
    // It creates MyEngine, sets the run values, and starts the simulation thread.
    public void startSimulation(SimulationConfig config) {
        // Create the engine with values from the selected config.
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

        // Set run length and speed, then start the engine thread.
        engine.setSimulationTime(config.simulationTime());

        engine.setDelay(config.delay());

        // Start the engine thread.
        ((Thread) engine).start();
    }

    /**
     * Makes the simulation slower by increasing the delay.
     */
    // SimulationPageController calls decreaseSpeed() when the user presses Slow Down button.
    // It increases delay so the simulation moves more slowly.
    public void decreaseSpeed() {
        // Slow down means a larger delay value.
        if (engine == null) {
            return;
        }

        // Increase delay by 20 percent to make the simulation slower.
        long newDelay = Math.max(0, Math.round(engine.getDelay() * 1.2));

        engine.setDelay(newDelay);

        // Wake the engine thread so the new delay is used right away.
        ((Thread) engine).interrupt();
    }

    /**
     * Makes the simulation faster by decreasing the delay.
     */
    // SimulationPageController calls increaseSpeed() when the user presses Speed Up button.
    // It decreases delay so the simulation moves more quickly.
    public void increaseSpeed() {
        // Speed up means a smaller delay value.
        if (engine == null) {
            return;
        }

        // Decrease delay by 20 percent to make the simulation faster.
        long newDelay = Math.max(0, Math.round(engine.getDelay() * 0.8));
        engine.setDelay(newDelay);

        // Wake the engine thread so the new delay is used right away.
        ((Thread) engine).interrupt();
    }

    /**
     * Pauses the simulation or resumes it if it is already paused.
     */
    // SimulationPageController calls this when the user presses Pause or Resume.
    // It changes the engine delay and updates the button text on the page.
    public void togglePause() {
        // Pause is done by storing the old delay and using a new very large delay.
        if (engine == null) {
            return;
        }

        isPaused = !isPaused;

        if (isPaused) {
            delayBeforePause = engine.getDelay();
            engine.setDelay(1000000); // ms
        } else {
            engine.setDelay(delayBeforePause);
            ((Thread) engine).interrupt();
        }

        // Change the button text with the current state.
        simulationPageController.setPauseButtonText(isPaused ? "Resume" : "Pause");
    }

    /**
     * Opens the results page after the engine finishes the simulation.
     *
     * @param time the final simulation time reported by the engine
     */
    // MyEngine calls showEndTime() when the simulation run ends.
    // It reads the final data from the engine and opens the results page.
    public void showEndTime(double time) {
        // Read final statistics from the engine and open the results page through showResultsPage.
        final StatisticsCollector.Snapshot snapshot = engine.getStatisticsSnapshot();
        final StatisticsAndMetricsRecord record = engine.getStatisticsRecord();

        // The engine finishes the simulation in its own thread, not in the JavaFX UI thread.
        // Platform.runLater() moves these UI updates to the JavaFX UI thread.
        Platform.runLater(() -> simulationPageController.showResultsPage(snapshot, record));
    }

    /**
     * Updates the timer and queue labels on the simulation page.
     */
    // MyEngine calls updateTimeAndQueues() this after a new arrival event.
    // It updates the timer and the queue labels on the simulation page.
    public void updateTimeAndQueues() {
        final int[] queueLengths = engine.getQueueLengths();

        // The engine updates queues in its own thread.
        // Platform.runLater() moves these UI updates to the JavaFX UI thread.
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
    // MyEngine calls updateOrderBook() after each cycle when a new order book snapshot is ready.
    // It sends that snapshot to the table of sells and buys on the simulation page.
    public void updateOrderBook(OrderBook.OrderBookSnapshot snapshot) {

        // The order book table is part of the UI,
        // so this update must also run in the JavaFX UI thread.
        Platform.runLater(() -> simulationPageController.showOrderBook(snapshot));
    }
}

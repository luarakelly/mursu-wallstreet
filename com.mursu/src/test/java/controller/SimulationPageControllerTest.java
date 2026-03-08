package controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("SimulationPageController tests")
class SimulationPageControllerTest {

    @Test
    @DisplayName("simulation page controller can be created")
    void simulationPageControllerCanBeCreated() {
        SimulationPageController controller = new SimulationPageController();
        assertNotNull(controller);
    }

    @Test
    @DisplayName("simulation page controller has update methods")
    void simulationPageControllerHasUpdateMethods() {
        assertDoesNotThrow(() -> {
            Method startSimulation = SimulationPageController.class.getDeclaredMethod("startSimulation", eds.config.SimulationConfig.class);
            Method updateSimulationTime = SimulationPageController.class.getDeclaredMethod("updateSimulationTime", double.class);
            Method updateQueueLengths = SimulationPageController.class.getDeclaredMethod("updateQueueLengths", int[].class);
            Method showOrderBook = SimulationPageController.class.getDeclaredMethod("showOrderBook", eds.model.OrderBook.OrderBookSnapshot.class);
            Method showResultsPage = SimulationPageController.class.getDeclaredMethod("showResultsPage", eds.model.StatisticsCollector.Snapshot.class, eds.database.Records.StatisticsAndMetricsRecord.class);

            assertNotNull(startSimulation);
            assertNotNull(updateSimulationTime);
            assertNotNull(updateQueueLengths);
            assertNotNull(showOrderBook);
            assertNotNull(showResultsPage);
        });
    }
}

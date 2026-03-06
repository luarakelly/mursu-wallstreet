package eds.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import controller.IControllerMtoV;
import eds.framework.Clock;

@DisplayName("MyEngine tests")
class MyEngineTest {

    static class DummyController implements IControllerMtoV {
        int showEndTimeCalls = 0;
        int visualiseCalls = 0;
        double latestEndTime = 0.0;

        @Override
        public void showEndTime(double time) {
            showEndTimeCalls++;
            latestEndTime = time;
        }

        @Override
        public void visualiseEntity() {
            visualiseCalls++;
        }
    }

    @Test
    @DisplayName("run creates statistics snapshot with required metrics")
    void runCreatesStatisticsSnapshot() {
        Clock.getInstance().setTime(0.0);

        DummyController controller = new DummyController();
        MyEngine engine = new MyEngine(
                controller,
                42L,
                0.3,
                0.3,
                0.3,
                0.3,
                0.2,
                100.0,
                0.2,
                0.01
        );

        engine.setDelay(0);
        engine.setSimulationTime(3.0);

        engine.run();

        StatisticsCollector.Snapshot snapshot = engine.getStatisticsSnapshot();

        assertNotNull(snapshot);
        assertTrue(snapshot.totalArrivedOrders() > 0);
        assertTrue(snapshot.totalExecutedOrders() >= 0);
        assertTrue(snapshot.remainingOrdersInBook() >= 0);
        assertTrue(snapshot.totalTrades() >= 0);
        assertTrue(snapshot.vwap() >= 0.0);
        assertTrue(snapshot.averageMidPrice() >= 0.0);
        assertTrue(snapshot.minPrice() >= 0.0);
        assertTrue(snapshot.maxPrice() >= 0.0);
        assertTrue(snapshot.averageSpread() >= 0.0);
        assertTrue(snapshot.throughput() >= 0.0);
        assertTrue(snapshot.averageWaitingTime() >= 0.0);
        assertTrue(snapshot.fillRate() >= 0.0 && snapshot.fillRate() <= 1.0);
        assertTrue(snapshot.averageServicePointUtilization() >= 0.0
                && snapshot.averageServicePointUtilization() <= 1.0);
        assertEquals(4, snapshot.servicePointUtilization().size());
        assertEquals(4, snapshot.averageQueueLengthPerServicePoint().size());

        assertEquals(1, controller.showEndTimeCalls);
        assertTrue(controller.latestEndTime >= 0.0);
    }
}

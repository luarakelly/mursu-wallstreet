package eds.framework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import controller.Controller;
import controller.SimulationPageController;
import eds.model.EventType;
import eds.model.Order;

@DisplayName("Engine tests")
class EngineTest {

    static class TestEngine extends Engine {
        int initializationCalls = 0;
        int eventCalls = 0;
        int resultCalls = 0;
        int afterCycleCalls = 0;

        TestEngine() {
            super(new Controller(new SimulationPageController()));
            servicePoints = new ServicePoint[0];
        }

        @Override
        protected void initialization() {
            initializationCalls++;
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 10, 0.0);
            eventList.add(new Event(EventType.ARRIVAL, 1.0, order));
        }

        @Override
        protected void runEvent(Event t) {
            eventCalls++;
            if (eventCalls == 1) {
                Order nextOrder = new Order(Order.Side.SELL, Order.Type.LIMIT, 101.0, 5, 0.0);
                eventList.add(new Event(EventType.ARRIVAL, 2.0, nextOrder));
            }
        }

        @Override
        protected void afterCycle() {
            afterCycleCalls++;
        }

        @Override
        protected void results() {
            resultCalls++;
        }
    }

    @Test
    @DisplayName("engine stores delay and simulation time values")
    void engineStoresDelayAndSimulationTimeValues() {
        TestEngine engine = new TestEngine();
        engine.setDelay(15);
        engine.setSimulationTime(2.5);

        assertNotNull(engine);
        assertEquals(15, engine.getDelay());
    }
}

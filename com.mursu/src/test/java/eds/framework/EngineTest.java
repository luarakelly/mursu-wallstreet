package eds.framework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import controller.IControllerMtoV;
import eds.model.EventType;
import eds.model.Order;

@DisplayName("Engine tests")
class EngineTest {

    static class DummyController implements IControllerMtoV {
        @Override
        public void showEndTime(double time) {
        }

        @Override
        public void visualiseEntity() {
        }
    }

    static class TestEngine extends Engine {
        int initializationCalls = 0;
        int eventCalls = 0;
        int resultCalls = 0;
        int afterCycleCalls = 0;

        TestEngine() {
            super(new DummyController());
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
    @DisplayName("run executes initialization, event processing, afterCycle, and results")
    void runExecutesSimulationLifecycle() {
        Clock.getInstance().setTime(0.0);

        TestEngine engine = new TestEngine();
        engine.setDelay(0);
        engine.setSimulationTime(1.0);

        engine.run();

        assertEquals(1, engine.initializationCalls);
        assertEquals(1, engine.eventCalls);
        assertEquals(1, engine.afterCycleCalls);
        assertEquals(1, engine.resultCalls);
        assertEquals(1.0, Clock.getInstance().getTime());
    }
}

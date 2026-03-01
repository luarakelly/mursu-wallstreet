package eds.framework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eds.model.EventType;
import eds.model.Order;
import eduni.distributions.ContinuousGenerator;
import eduni.distributions.DiscreteGenerator;

@DisplayName("ArrivalProcess tests :')")
class ArrivalProcessTest {

    // Generating test values
    static class FixedContinuous implements ContinuousGenerator {
        private final double value;

        FixedContinuous(double value) {
            this.value = value;
        }

        @Override
        public double sample() {
            return value;
        }

        @Override
        public void setSeed(long seed) {
        }

        @Override
        public long getSeed() {
            return 1L;
        }

        @Override
        public void reseed() {
        }
    }

    // Discrete generator.
    static class FixedDiscrete implements DiscreteGenerator {
        private final long value;

        FixedDiscrete(long value) {
            this.value = value;
        }

        @Override
        public long sample() {
            return value;
        }

        @Override
        public void setSeed(long seed) {
        }

        @Override
        public long getSeed() {
            return 1L;
        }

        @Override
        public void reseed() {
        }
    }

    @Test
    @DisplayName("generateNext adds ARRIVAL event to event list")
    
    void generateNextAddsEvent() {
        // Arrange
        EventList eventList = new EventList();
        Clock.getInstance().setTime(10.0);

        ArrivalProcess process = new ArrivalProcess(
                new FixedContinuous(2.0),
                eventList,
                EventType.ARRIVAL,
                new FixedDiscrete(1),
                new FixedDiscrete(1),
                new FixedContinuous(0.0),
                new FixedDiscrete(5),
                100.0,
                0.01
        );

        /* Generate next value deterministically */
        process.generateNext();
        assertFalse(eventList.isEmpty());
        assertEquals(12.0, eventList.getNextTime());
    }

    @Test
    @DisplayName("generateNext creates order from generators")
    void generateNextCreatesOrderFromGenerators() {
        EventList eventList = new EventList();
        Clock.getInstance().setTime(5.0);

        ArrivalProcess process = new ArrivalProcess(
                new FixedContinuous(1.0),
                eventList,
                EventType.ARRIVAL,
                new FixedDiscrete(0),
                new FixedDiscrete(1),
                new FixedContinuous(0.0),
                new FixedDiscrete(20),
                100.0,
                0.01
        );

        process.generateNext();
        Event event = eventList.remove();
        Order order = event.getOrder();

        assertEquals(EventType.ARRIVAL, event.getType());
        assertNotNull(order);
        assertEquals(Order.Side.SELL, order.getSide());
        assertEquals(Order.Type.LIMIT, order.getType());
        assertEquals(20, order.getOriginalShareSize());
    }
}

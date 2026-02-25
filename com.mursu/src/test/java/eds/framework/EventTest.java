package eds.framework;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eds.model.EventType;
import eds.model.Order;

class EventTest {
    @Test
    void constructorStoresValuesCorrectly() {
        IEventType type = EventType.ARRIVAL;
        Order order = null;

        Event event = new Event(type, 5.0, order);

        assertEquals(type, event.getType());
        assertEquals(5.0, event.getTime());
        assertEquals(null, event.getOrder());
    }

    @Test
    void compareToOrdersByTime() {
        Event event1 = new Event(EventType.ARRIVAL, 5.0, null);
        Event event2 = new Event(EventType.ARRIVAL, 10.0, null);

        assertTrue(event1.compareTo(event2) < 0);
    }
}
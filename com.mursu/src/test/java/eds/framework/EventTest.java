package eds.framework;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;

import eds.model.EventType;
import eds.framework.ISimulationEntity;

@DisplayName("Event tests")
class EventTest {
    @Test
    @DisplayName("constructor correctly stores type, time and entity")
    void constructorStoresValuesCorrectly() {
        IEventType type = EventType.ARRIVAL;
        ISimulationEntity entity = null;

        Event event = new Event(type, 5.0, entity);

        assertEquals(type, event.getType());
        assertEquals(5.0, event.getTime());
        assertEquals(null, event.getEntity());
    }

    @Test
    @DisplayName("compareTo returns negative number (aka false) when first event time is smaller")
    void compareToOrdersByTime() {
        Event event1 = new Event(EventType.ARRIVAL, 5.0, null);
        Event event2 = new Event(EventType.ARRIVAL, 10.0, null);

        assertTrue(event1.compareTo(event2) < 0);
    }
}

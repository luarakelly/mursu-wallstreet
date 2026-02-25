package eds.framework;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eds.model.EventType;

class EventListTest {
    @Test
    void eventsAreReturnedInTimeOrder() {
        EventList list = new EventList();

        list.add(new Event(EventType.ARRIVAL, 10.0, null));
        list.add(new Event(EventType.ARRIVAL, 5.0, null));

        Event first = list.remove();

        assertEquals(5.0, first.getTime());
    }

    @Test
    void isEmptyWorks() {
        EventList list = new EventList();

        assertTrue(list.isEmpty());

        list.add(new Event(EventType.ARRIVAL, 1.0, null));

        list.remove();

        assertTrue(list.isEmpty());
    }

    @Test
    void getNextTimeThrowsIfEmpty() {
        EventList list = new EventList();

        assertThrows(IllegalStateException.class, list::getNextTime);
    }
}
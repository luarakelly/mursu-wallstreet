package eds.framework;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;

import eds.model.EventType;

@DisplayName("EventList tests")
class EventListTest {
    @DisplayName("events are returned in time order")
    @Test
    void eventsAreReturnedInTimeOrder() {
        EventList list = new EventList();

        list.add(new Event(EventType.ARRIVAL, 10.0, null));
        list.add(new Event(EventType.ARRIVAL, 5.0, null));

        Event first = list.remove();

        assertEquals(5.0, first.getTime());
    }

    @DisplayName("isEmpty returns correct state")
    @Test
    void isEmptyWorks() {
        EventList list = new EventList();

        assertTrue(list.isEmpty());

        list.add(new Event(EventType.ARRIVAL, 1.0, null));

        list.remove();

        assertTrue(list.isEmpty());
    }

    @DisplayName("getNextTime throws error when list is empty")
    @Test
    void getNextTimeThrowsErrorIfEmpty() {
        EventList list = new EventList();

        assertThrows(IllegalStateException.class, list::getNextTime);
    }
}
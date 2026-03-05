package eds.framework;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;

import eds.model.EventType;
import eds.model.Order;
import eduni.distributions.Negexp;

@DisplayName("ServicePoint tests")
class ServicePointTest {
    static class FixedNegexp extends Negexp {
        private final double value;

        public FixedNegexp(double value) {
            super(1.0);
            this.value = value;
        }

        public double sample() {
            return value;
        }
    }

    @DisplayName("add queues entity, startService creates completion event")
    @Test
    void addQueuesAndStartServiceCreatesEvent() {
        EventList eventList = new EventList();
        FixedNegexp generator = new FixedNegexp(5.0);

        ServicePoint servicePoint = new ServicePoint(generator, eventList, EventType.VALIDATION_COMPLETE);

        Order order = null;
        Clock.getInstance().setTime(10.0);

        servicePoint.add(order);

        assertFalse(servicePoint.isBusy());
        assertTrue(servicePoint.hasOrders());
        assertTrue(eventList.isEmpty());

        servicePoint.startService();

        assertTrue(servicePoint.isBusy());
        assertFalse(eventList.isEmpty());
        assertEquals(15.0, eventList.getNextTime());
    }

    @DisplayName("finishService releases current entity, next requires explicit start")
    @Test
    void finishServiceReleasesAndRequiresExplicitNextStart() {
        EventList eventList = new EventList();
        FixedNegexp generator = new FixedNegexp(2.0);

        ServicePoint servicePoint = new ServicePoint(generator, eventList, EventType.VALIDATION_COMPLETE);

        Order order1 = null;
        Order order2 = null;

        Clock.getInstance().setTime(0.0);

        servicePoint.add(order1);
        servicePoint.add(order2);
        servicePoint.startService();

        servicePoint.finishService();

        assertFalse(servicePoint.isBusy());
        assertTrue(servicePoint.hasOrders());

        servicePoint.startService();
        assertTrue(servicePoint.isBusy());
    }

    @DisplayName("finishService makes service point idle when queue is empty")
    @Test
    void finishServiceWhenNoOrdersMakesIdle() {
        EventList eventList = new EventList();
        FixedNegexp generator = new FixedNegexp(1.0);

        ServicePoint servicePoint = new ServicePoint(generator, eventList, EventType.VALIDATION_COMPLETE);

        Order order1 = null;
        Clock.getInstance().setTime(0.0);

        servicePoint.add(order1);
        servicePoint.startService();
        servicePoint.finishService();

        assertFalse(servicePoint.isBusy());
    }
}
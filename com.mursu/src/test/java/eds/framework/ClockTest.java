package eds.framework;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClockTest {
    @Test
    void returnsSameInstance() {
        Clock clock1 = Clock.getInstance();
        Clock clock2 = Clock.getInstance();

        assertSame(clock1, clock2);
    }

    @Test
    void setAndGetTimeWorks() {
        Clock clock = Clock.getInstance();
        clock.setTime(42.0);

        assertEquals(42.0, clock.getTime());
    }
}
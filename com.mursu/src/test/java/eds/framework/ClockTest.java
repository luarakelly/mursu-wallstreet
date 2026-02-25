package eds.framework;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Clock tests")
class ClockTest {
    @Test
    @DisplayName("returns same instance")
    void returnsSameInstance() {
        Clock clock1 = Clock.getInstance();
        Clock clock2 = Clock.getInstance();

        assertSame(clock1, clock2);
    }

    @Test
    @DisplayName("time setter and getter work")
    void setAndGetTimeWorks() {
        Clock clock = Clock.getInstance();
        clock.setTime(42.0);

        assertEquals(42.0, clock.getTime());
    }
}
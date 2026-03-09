package controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Controller tests")
class ControllerTest {

    @Test
    @DisplayName("controller can be created for simulation page")
    void controllerCanBeCreated() {
        Controller controller = new Controller(new SimulationPageController());
        assertNotNull(controller);
    }

    @Test
    @DisplayName("controller has main simulation methods")
    void controllerHasMainSimulationMethods() {
        assertDoesNotThrow(() -> {
            Method startSimulation = Controller.class.getDeclaredMethod("startSimulation", eds.config.SimulationConfig.class);
            Method decreaseSpeed = Controller.class.getDeclaredMethod("decreaseSpeed");
            Method increaseSpeed = Controller.class.getDeclaredMethod("increaseSpeed");
            Method togglePause = Controller.class.getDeclaredMethod("togglePause");
            Method updateOrderBook = Controller.class.getDeclaredMethod("updateOrderBook", eds.model.OrderBook.OrderBookSnapshot.class);

            assertNotNull(startSimulation);
            assertNotNull(decreaseSpeed);
            assertNotNull(increaseSpeed);
            assertNotNull(togglePause);
            assertNotNull(updateOrderBook);
        });
    }
}

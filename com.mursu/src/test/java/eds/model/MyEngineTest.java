package eds.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import controller.Controller;
import controller.SimulationPageController;

@DisplayName("MyEngine tests")
class MyEngineTest {

    @Test
    @DisplayName("my engine can be created with simulation controller")
    void myEngineCanBeCreated() {
        Controller controller = new Controller(new SimulationPageController());
        MyEngine engine = new MyEngine(
                controller,
                42L,
                0.3,
                0.3,
                0.3,
                0.3,
                0.2,
                0.5,
                0.5,
                100.0,
                0.2,
                0.01,
                "Test Run");

        assertNotNull(engine);
    }
}

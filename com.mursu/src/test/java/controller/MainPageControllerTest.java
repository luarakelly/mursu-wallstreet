package controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("MainPageController tests")
class MainPageControllerTest {

    @Test
    @DisplayName("main page controller can be created")
    void mainPageControllerCanBeCreated() {
        MainPageController controller = new MainPageController();
        assertNotNull(controller);
    }

    @Test
    @DisplayName("main page controller has start and preset methods")
    void mainPageControllerHasMainMethods() {
        assertDoesNotThrow(() -> {
            Method handleStartSimulation = MainPageController.class.getDeclaredMethod("handleStartSimulation");
            Method applyBalancedPreset = MainPageController.class.getDeclaredMethod("applyBalancedPreset");
            Method applySlowPreset = MainPageController.class.getDeclaredMethod("applySlowPreset");
            Method applyHighFrequencyPreset = MainPageController.class.getDeclaredMethod("applyHighFrequencyPreset");
            Method applyVolatilePreset = MainPageController.class.getDeclaredMethod("applyVolatilePreset");

            assertNotNull(handleStartSimulation);
            assertNotNull(applyBalancedPreset);
            assertNotNull(applySlowPreset);
            assertNotNull(applyHighFrequencyPreset);
            assertNotNull(applyVolatilePreset);
        });
    }
}

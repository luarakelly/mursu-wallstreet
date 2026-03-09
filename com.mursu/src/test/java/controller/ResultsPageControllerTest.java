package controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("ResultsPageController tests")
class ResultsPageControllerTest {

    @Test
    @DisplayName("results page controller can be created")
    void resultsPageControllerCanBeCreated() {
        ResultsPageController controller = new ResultsPageController();
        assertNotNull(controller);
    }

    @Test
    @DisplayName("results page controller has result methods")
    void resultsPageControllerHasResultMethods() {
        assertDoesNotThrow(() -> {
            Method setResults = ResultsPageController.class.getDeclaredMethod("setResults", eds.model.StatisticsCollector.Snapshot.class, eds.database.Records.StatisticsAndMetricsRecord.class);
            Method handleBackToMain = ResultsPageController.class.getDeclaredMethod("handleBackToMain");
            Method handleDownloadCsv = ResultsPageController.class.getDeclaredMethod("handleDownloadCsv");

            assertNotNull(setResults);
            assertNotNull(handleBackToMain);
            assertNotNull(handleDownloadCsv);
        });
    }
}

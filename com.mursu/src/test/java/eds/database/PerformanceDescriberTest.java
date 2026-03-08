package eds.database;

import eds.database.Records.StatisticsAndMetricsRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PerformanceDescriber tests")
class PerformanceDescriberTest {

    private StatisticsAndMetricsRecord createRecord(
            double fillRate,
            double avgSpread,
            double vwap,
            double utilizationExecution,
            double avgQueueValidation,
            double avgQueueMarket,
            double avgQueueLimit,
            double avgQueueExecution,
            double utilizationValidation,
            double utilizationMarket,
            double utilizationLimit) {
        return new StatisticsAndMetricsRecord(
                1,
                "2026-03-08 12:00:00",
                "Test Run",
                42L,
                0.3,
                0.3,
                0.3,
                0.3,
                0.2,
                10.0,
                100,
                50,
                40,
                2,
                10,
                vwap,
                100.0,
                99.0,
                101.0,
                avgSpread,
                1.0,
                1.0,
                fillRate,
                utilizationValidation,
                utilizationMarket,
                utilizationLimit,
                utilizationExecution,
                avgQueueValidation,
                avgQueueMarket,
                avgQueueLimit,
                avgQueueExecution
        );
    }

    @Test
    @DisplayName("generateInsights returns non-empty text for good run")
    void generateInsightsReturnsTextForGoodRun() {
        StatisticsAndMetricsRecord record = createRecord(
                0.85,
                0.1,
                10.0,
                0.5,
                0.2,
                0.2,
                0.2,
                0.2,
                0.5,
                0.5,
                0.5
        );

        PerformanceDescriber describer = new PerformanceDescriber(record);
        List<String> insights = describer.generateInsights();

        assertFalse(insights.isEmpty());
        assertTrue(insights.get(0).contains("Fill rate is"));
    }

    @Test
    @DisplayName("generateInsights mentions market matching queue when it is largest")
    void generateInsightsMentionsLargestQueue() {
        StatisticsAndMetricsRecord record = createRecord(
                0.60,
                0.1,
                10.0,
                0.9,
                0.2,
                3.0,
                0.2,
                0.2,
                0.5,
                0.9,
                0.4
        );

        PerformanceDescriber describer = new PerformanceDescriber(record);
        List<String> insights = describer.generateInsights();

        boolean queueTextFound = false;
        for (String insight : insights) {
            if (insight.contains("market matching")) {
                queueTextFound = true;
                break;
            }
        }

        assertTrue(queueTextFound);
    }

    @Test
    @DisplayName("describe returns summary text")
    void describeReturnsSummary() {
        StatisticsAndMetricsRecord record = createRecord(
                0.82,
                0.2,
                10.0,
                0.4,
                0.2,
                0.2,
                0.2,
                0.2,
                0.5,
                0.5,
                0.5
        );

        PerformanceDescriber describer = new PerformanceDescriber(record);
        String summary = describer.describe();

        assertNotNull(summary);
        assertFalse(summary.isBlank());
    }
}

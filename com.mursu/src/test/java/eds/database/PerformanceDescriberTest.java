package eds.database;

import eds.database.Records.StatisticsAndMetricsRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerformanceDescriberTest {

    @Test
    @DisplayName("generateInsights detects strong liquidity")
    void generateInsights_detectsStrongLiquidity() {

        StatisticsAndMetricsRecord record = mock(StatisticsAndMetricsRecord.class);

        when(record.fillRate()).thenReturn(0.85);
        when(record.avgSpread()).thenReturn(0.1);
        when(record.vwap()).thenReturn(10.0);
        when(record.utilizationExecution()).thenReturn(0.5);
        when(record.avgQueueValidation()).thenReturn(0.2);
        when(record.avgQueueMarket()).thenReturn(0.2);
        when(record.avgQueueLimit()).thenReturn(0.2);
        when(record.avgQueueExecution()).thenReturn(0.2);
        when(record.utilizationValidation()).thenReturn(0.5);
        when(record.utilizationMarket()).thenReturn(0.5);
        when(record.utilizationLimit()).thenReturn(0.5);

        PerformanceDescriber describer = new PerformanceDescriber(record);

        List<String> insights = describer.generateInsights();

        assertFalse(insights.isEmpty());
        assertTrue(insights.get(0).contains("strong liquidity"));
    }

    @Test
    @DisplayName("generateInsights detects bottleneck in market matching")
    void generateInsights_detectsBottleneck() {

        StatisticsAndMetricsRecord record = mock(StatisticsAndMetricsRecord.class);

        when(record.fillRate()).thenReturn(0.6);
        when(record.avgSpread()).thenReturn(0.1);
        when(record.vwap()).thenReturn(10.0);
        when(record.utilizationExecution()).thenReturn(0.9);

        when(record.avgQueueValidation()).thenReturn(0.2);
        when(record.avgQueueMarket()).thenReturn(3.0); // bottleneck
        when(record.avgQueueLimit()).thenReturn(0.2);
        when(record.avgQueueExecution()).thenReturn(0.2);

        when(record.utilizationValidation()).thenReturn(0.5);
        when(record.utilizationMarket()).thenReturn(0.9);
        when(record.utilizationLimit()).thenReturn(0.4);

        PerformanceDescriber describer = new PerformanceDescriber(record);

        List<String> insights = describer.generateInsights();

        boolean bottleneckFound = false;

        for (String insight : insights) {
            if (insight.contains("Bottleneck")) {
                bottleneckFound = true;
                break;
            }
        }

        assertTrue(bottleneckFound);
    }

    @Test
    @DisplayName("describe returns summary text")
    void describe_returnsSummary() {

        StatisticsAndMetricsRecord record = mock(StatisticsAndMetricsRecord.class);

        when(record.fillRate()).thenReturn(0.82);
        when(record.avgSpread()).thenReturn(0.2);
        when(record.vwap()).thenReturn(10.0);
        when(record.utilizationExecution()).thenReturn(0.4);

        PerformanceDescriber describer = new PerformanceDescriber(record);

        String summary = describer.describe();

        assertNotNull(summary);
        assertTrue(summary.contains("Simulation shows"));
    }
}
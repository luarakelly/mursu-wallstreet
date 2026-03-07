package eds.database;

import java.util.ArrayList;
import java.util.List;
import eds.database.Records.StatisticsAndMetricsRecord;

/**
 * A PerformanceDescriber converts raw simulation metrics from {@link StatisticsAndMetricsRecord}
 * into human‑readable descriptions.
 */

public class PerformanceDescriber {

    // record contains all statistics variables from one simulation run
    private StatisticsAndMetricsRecord record;

    public PerformanceDescriber(StatisticsAndMetricsRecord record) {
        this.record = record;
    }

    /**
     * Generates human‑readable insights from the simulation metrics.
     *
     * @return list of observations and warnings for the simulation run
     */
    public List<String> generateInsights() {
        List<String> insights = new ArrayList<>();

        double fillRate = record.fillRate();
        if (fillRate <= 1) {
            fillRate *= 100;
        }

        if (fillRate >= 80) {
            insights.add("Fill rate is " + String.format("%.1f", fillRate) +
                    "% - strong liquidity, most orders matched.");
        } else if (fillRate >= 50) {
            insights.add("Fill rate is " + String.format("%.1f", fillRate) +
                    "% - moderate liquidity.");
        } else {
            insights.add("Fill rate is " + String.format("%.1f", fillRate) +
                    "% - weak liquidity.");
        }

        double spread = record.avgSpread();
        double vwap = record.vwap();

        if (vwap > 0 && spread > vwap * 0.02) {
            insights.add("Average spread of " + String.format("%.4f", spread) +
                    " is wide relative to VWAP of " + String.format("%.4f", vwap) +
                    " - thin liquidity.");
        } else if (vwap > 0) {
            insights.add("Average spread of " + String.format("%.4f", spread) +
                    " stays reasonable relative to VWAP of " + String.format("%.4f", vwap) +
                    " - pricing looks stable.");
        } else {
            insights.add("Average spread is " + String.format("%.4f", spread) +
                    " - not enough trade price data for VWAP comparison.");
        }

        double execUtil = record.utilizationExecution();
        if (execUtil <= 1) {
            execUtil *= 100;
        }

        if (execUtil > 95) {
            insights.add("Execution service point is at " +
                    String.format("%.1f", execUtil) +
                    "% utilization - system near saturation.");
        } else if (execUtil > 75) {
            insights.add("Execution service point is at " +
                    String.format("%.1f", execUtil) +
                    "% utilization - high load but still stable.");
        } else {
            insights.add("Execution service point is at " +
                    String.format("%.1f", execUtil) +
                    "% utilization - load is under control.");
        }

        double qValidation = record.avgQueueValidation();
        double qMarket = record.avgQueueMarket();
        double qLimit = record.avgQueueLimit();
        double qExecution = record.avgQueueExecution();

        double uValidation = record.utilizationValidation();
        double uMarket = record.utilizationMarket();
        double uLimit = record.utilizationLimit();
        double uExecution = execUtil;

        // convert utilization to percentage
        if (uValidation <= 1) uValidation *= 100;
        if (uMarket <= 1) uMarket *= 100;
        if (uLimit <= 1) uLimit *= 100;

        // choose the stage with the largest queue
        double maxQueue = qValidation;
        String bottleneck = "validation";
        double util = uValidation;

        if (qMarket > maxQueue) {
            maxQueue = qMarket;
            bottleneck = "market matching";
            util = uMarket;
        }

        if (qLimit > maxQueue) {
            maxQueue = qLimit;
            bottleneck = "limit matching";
            util = uLimit;
        }

        if (qExecution > maxQueue) {
            maxQueue = qExecution;
            bottleneck = "execution";
            util = uExecution;
        }

        insights.add("Largest average queue is at " + bottleneck +
                ". Average queue size is " + String.format("%.2f", maxQueue) + " orders.");

        return insights;
    }

    /**
     * Generates a short human‑readable summary of the simulation run.
     *
     * @return summary description of overall simulation performance
     */
    public String describe() {

        double fillRate = record.fillRate();
        if (fillRate <= 1) {
            fillRate *= 100;
        }

        double spread = record.avgSpread();
        double vwap = record.vwap();

        double execUtil = record.utilizationExecution();
        if (execUtil <= 1) {
            execUtil *= 100;
        }

        String liquidity;
        if (fillRate >= 80) {
            liquidity = "strong liquidity";
        } else if (fillRate >= 50) {
            liquidity = "moderate liquidity";
        } else {
            liquidity = "weak liquidity";
        }

        String spreadState = "normal spreads";
        if (vwap > 0 && spread > vwap * 0.02) {
            spreadState = "wide spreads";
        }

        String loadState = "normal system load";
        if (execUtil > 95) {
            loadState = "execution stage near saturation";
        }

        return "Simulation shows " + liquidity + ", " + spreadState + ", and " + loadState + ".";
    }
}

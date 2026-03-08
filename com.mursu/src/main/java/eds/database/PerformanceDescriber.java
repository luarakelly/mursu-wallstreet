package eds.database;

import java.util.ArrayList;
import java.util.List;
import eds.database.Records.StatisticsAndMetricsRecord;

/**
 * A PerformanceDescriber converts raw simulation metrics from {@link StatisticsAndMetricsRecord}
 * into human-readable descriptions.
 */
public class PerformanceDescriber {

    // record contains all statistics variables from one simulation run
    private StatisticsAndMetricsRecord record;

    public PerformanceDescriber(StatisticsAndMetricsRecord record) {
        this.record = record;
    }

    /**
     * Generates human-readable insights from the simulation metrics.
     *
     * @return list of observations and warnings for the simulation run
     */
    public List<String> generateInsights() {
        List<String> insights = new ArrayList<>();

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

        double qValidation = record.avgQueueValidation();
        double qMarket = record.avgQueueMarket();
        double qLimit = record.avgQueueLimit();
        double qExecution = record.avgQueueExecution();

        double maxQueue = qValidation;
        String largestQueue = "validation";

        if (qMarket > maxQueue) {
            maxQueue = qMarket;
            largestQueue = "market matching";
        }
        if (qLimit > maxQueue) {
            maxQueue = qLimit;
            largestQueue = "limit matching";
        }
        if (qExecution > maxQueue) {
            maxQueue = qExecution;
            largestQueue = "execution";
        }

        boolean weakFillRate = fillRate < 50;
        boolean moderateFillRate = fillRate < 70;
        boolean wideSpread = vwap > 0 && spread > vwap * 0.02;
        boolean moderatelyWideSpread = vwap > 0 && spread > vwap * 0.01;
        boolean saturatedExecution = execUtil > 95;
        boolean heavyExecutionLoad = execUtil > 85;
        boolean queueBuildUp = maxQueue >= 1.0;

        if (weakFillRate) {
            insights.add("Fill rate is " + String.format("%.1f", fillRate) +
                    "% - matching is clearly weak for this run.");
        } else if (moderateFillRate) {
            insights.add("Fill rate is " + String.format("%.1f", fillRate) +
                    "% - matching looks somewhat limited.");
        } else if (fillRate < 85) {
            insights.add("Fill rate is " + String.format("%.1f", fillRate) +
                    "% - a reasonable share of orders found a match.");
        } else {
            insights.add("Fill rate is " + String.format("%.1f", fillRate) +
                    "% - most orders found a match.");
        }

        if (wideSpread) {
            insights.add("Average spread is " + String.format("%.4f", spread) +
                    " relative to VWAP " + String.format("%.4f", vwap) +
                    " - liquidity looks thin.");
        } else if (moderatelyWideSpread) {
            insights.add("Average spread is " + String.format("%.4f", spread) +
                    " relative to VWAP " + String.format("%.4f", vwap) +
                    " - pricing is somewhat wide.");
        } else if (vwap > 0) {
            insights.add("Average spread is " + String.format("%.4f", spread) +
                    " relative to VWAP " + String.format("%.4f", vwap) +
                    ".");
        } else {
            insights.add("Average spread is " + String.format("%.4f", spread) +
                    " - VWAP is not available for comparison.");
        }

        if (saturatedExecution) {
            insights.add("Execution utilization is " + String.format("%.1f", execUtil) +
                    "% - the execution stage is near saturation.");
        } else if (heavyExecutionLoad) {
            insights.add("Execution utilization is " + String.format("%.1f", execUtil) +
                    "% - the execution stage is under noticeable load.");
        } else {
            insights.add("Execution utilization is " + String.format("%.1f", execUtil) + "%.");
        }

        if (queueBuildUp) {
            insights.add("Largest average queue is at " + largestQueue +
                    ". Average queue size is " + String.format("%.2f", maxQueue) +
                    " orders - some accumulation is visible there.");
        } else {
            insights.add("Largest average queue is at " + largestQueue +
                    ". Average queue size is " + String.format("%.2f", maxQueue) + " orders.");
        }

        return insights;
    }

    /**
     * Generates a short human-readable summary of the simulation run.
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

        double qValidation = record.avgQueueValidation();
        double qMarket = record.avgQueueMarket();
        double qLimit = record.avgQueueLimit();
        double qExecution = record.avgQueueExecution();
        double maxQueue = Math.max(Math.max(qValidation, qMarket), Math.max(qLimit, qExecution));

        boolean weakFillRate = fillRate < 50;
        boolean moderateFillRate = fillRate < 70;
        boolean wideSpread = vwap > 0 && spread > vwap * 0.02;
        boolean moderatelyWideSpread = vwap > 0 && spread > vwap * 0.01;
        boolean saturatedExecution = execUtil > 95;
        boolean heavyExecutionLoad = execUtil > 85;
        boolean seriousQueueBuildUp = maxQueue >= 3.0;
        boolean moderateQueueBuildUp = maxQueue >= 1.0;

        if (weakFillRate || wideSpread || saturatedExecution || seriousQueueBuildUp) {
            return "Simulation finished with mixed results and some signs of market stress or congestion.";
        }

        if (moderateFillRate || moderatelyWideSpread || heavyExecutionLoad || moderateQueueBuildUp) {
            return "Simulation finished with moderate results and noticeable pressure in some metrics.";
        }

        return "Simulation finished without major warning signs in the main metrics.";
    }
}

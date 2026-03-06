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

        // FILL RATE - how many orders were successfully matched.
        // High fill rate means good liquidity in the market.
        double fillRate = record.fillRate();
        if (fillRate <= 1) {
            fillRate *= 100;
        }

        if (fillRate >= 80) {
            insights.add("Fill rate is " + String.format("%.1f", fillRate) +
                    "% – strong liquidity, most orders matched.");
        }
        else if (fillRate >= 50) {
            insights.add("Fill rate is " + String.format("%.1f", fillRate) +
                    "% – moderate liquidity.");
        }
        else {
            insights.add("Fill rate is " + String.format("%.1f", fillRate) +
                    "% – weak liquidity.");
        }

        // SPREAD VS VWAP (Volume Weighted Average Price)
        // Spread - difference between best buy price and best sell price.
        // VWAP - average trade price weighted by volume.
        // If spread is large compared to VWAP, the market has thin liquidity.
        double spread = record.avgSpread();
        double vwap = record.vwap();

        if (vwap > 0 && spread > vwap * 0.02) {
            insights.add("Average spread of " + String.format("%.4f", spread) +
                    " is wide relative to VWAP of " + String.format("%.4f", vwap) +
                    " - thin liquidity.");
        }

        // EXECUTION UTILIZATION
        // - how busy the execution service point was.
        // Very high utilization means the system may be close to overload.
        double execUtil = record.utilizationExecution();
        if (execUtil <= 1) {
            execUtil *= 100;
        }

        if (execUtil > 95) {
            insights.add("Execution service point is at " +
                    String.format("%.1f", execUtil) +
                    "% utilization – system near saturation.");
        }

        // BOTTLENECK DETECTION
        // Each service point has a queue of orders waiting to be processed.
        // The biggest queue usually shows where the system slows down.
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

        // call it a bottleneck only if queue is noticeable and utilization is high
        if (maxQueue > 1 && util > 80) {
            insights.add("Bottleneck likely at " + bottleneck +
                    " – queue is growing and the service point is heavily loaded.");
        }

        // If none of the conditions above triggered,
        // it means the run looked normal and no warnings were detected.
        if (insights.isEmpty()) {
            insights.add("Run completed – no major warnings detected.");
        }

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

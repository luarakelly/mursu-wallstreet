package eds.database;

public class Records {

    public record StatisticsAndMetricsRecord(
            int id,
            String runTimestamp,
            String runName,
            long seed,
            double meanValidation,
            double meanMarket,
            double meanLimit,
            double meanExecution,
            double meanArrival,
            double simulationTime,
            int totalOrders,
            int totalTrades,
            int filledOrders,
            int cancelledOrders,
            int remainingOrders,
            double vwap,
            double avgMidPrice,
            double minPrice,
            double maxPrice,
            double avgSpread,
            double avgLatency,
            double throughput,
            double fillRate,
            double utilizationValidation,
            double utilizationMarket,
            double utilizationLimit,
            double utilizationExecution,
            double avgQueueValidation,
            double avgQueueMarket,
            double avgQueueLimit,
            double avgQueueExecution) {
    }

    public record TradeRecord(
            String id,
            int runId,
            String buyOrderId,
            String sellOrderId,
            double price,
            int shareSize,
            double conclusionTime) {
    }
}

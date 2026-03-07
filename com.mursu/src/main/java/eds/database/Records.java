package eds.database;

/**
 * Records — data transfer objects for simulation run persistence.
 *
 * Contains all record types used to move data between the model layer
 * and the database layer.
 */
public class Records {

        /**
         * Holds all statistics and metrics for a single simulation run.
         *
         * @param id                    auto-generated database primary key
         * @param runTimestamp          date and time the run was saved
         * @param runName               optional user-defined label for the run
         * @param seed                  random seed used for reproducibility
         * @param meanValidation        mean service time at the validation stage
         * @param meanMarket            mean service time at the market matching stage
         * @param meanLimit             mean service time at the limit matching stage
         * @param meanExecution         mean service time at the execution stage
         * @param meanArrival           mean inter-arrival time between orders
         * @param simulationTime        total simulated time elapsed
         * @param totalOrders           total number of orders that arrived
         * @param totalTrades           total number of trades executed
         * @param filledOrders          number of orders fully matched
         * @param cancelledOrders       number of orders that left unfilled
         * @param remainingOrders       number of orders still in the order book at end
         * @param vwap                  volume-weighted average trade price
         * @param avgMidPrice           average mid-price across the simulation
         * @param minPrice              lowest trade price recorded
         * @param maxPrice              highest trade price recorded
         * @param avgSpread             average bid-ask spread
         * @param avgLatency            average time from order arrival to execution
         * @param throughput            orders processed per unit of simulation time
         * @param fillRate              fraction of orders successfully filled (0.0–1.0)
         * @param utilizationValidation fraction of time the validation stage was busy
         * @param utilizationMarket     fraction of time the market matching stage was
         *                              busy
         * @param utilizationLimit      fraction of time the limit matching stage was
         *                              busy
         * @param utilizationExecution  fraction of time the execution stage was busy
         * @param avgQueueValidation    average queue length at the validation stage
         * @param avgQueueMarket        average queue length at the market matching
         *                              stage
         * @param avgQueueLimit         average queue length at the limit matching stage
         * @param avgQueueExecution     average queue length at the execution stage
         */
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

        /**
         * Holds the details of a single executed trade, linked to a simulation run.
         *
         * @param id             unique trade identifier
         * @param runId          id of the simulation run this trade belongs to
         * @param buyOrderId     id of the buy order involved in the trade
         * @param sellOrderId    id of the sell order involved in the trade
         * @param price          price at which the trade was executed
         * @param shareSize      number of shares exchanged
         * @param conclusionTime simulation time at which the trade was concluded
         */
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

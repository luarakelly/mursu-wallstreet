package eds.database;

import eds.database.Records.StatisticsAndMetricsRecord;
import eds.database.Records.TradeRecord;
import eds.model.StatisticsCollector;

import java.util.List;
import java.util.Optional;

/**
 * Read/write contract for simulation run persistence.
 * StatisticsQueries is the only implementation.
 * Controller and StatisticsCollector depend on this interface, not the concrete
 * class.
 */
public interface IQueries {
    /**
     * Converts a Snapshot and engine config parameters into a
     * StatisticsAndMetricsRecord ready for persistence.
     */
    StatisticsAndMetricsRecord buildRecord(
            StatisticsCollector.Snapshot snap,
            String runName,
            long seed,
            double meanValidation,
            double meanMarket,
            double meanLimit,
            double meanExecution,
            double meanArrival,
            double simulationTime);

    /**
     * Persists a completed simulation run.
     * 
     * @return the generated run id, or -1 if insert failed
     */
    int saveStatisticsAndMetrics(StatisticsAndMetricsRecord record);

    /**
     * Persists all trades from a run in a single transaction.
     * Linked to the run via runId returned by save().
     */
    int saveAllTrades(List<TradeRecord> trades, int runId);

    /**
     * Returns all simulation runs, most recent first.
     */
    List<StatisticsAndMetricsRecord> findAll();

    /**
     * Returns a single run by id, or Optional.empty() if not found.
     */
    Optional<StatisticsAndMetricsRecord> findById(int id);

    /**
     * Returns all trades belonging to a run, ordered by conclusion time.
     */
    List<TradeRecord> findTradesByRunId(int runId);
}
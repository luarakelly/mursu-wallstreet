package eds.database;

import eds.model.Trade;
import eds.database.Records.StatisticsAndMetricsRecord;
import eds.database.Records.TradeRecord;

import java.util.List;

/**
 * Read/write contract for simulation run persistence.
 * StatisticsQueries is the only implementation.
 * Controller and StatisticsCollector depend on this interface, not the concrete
 * class.
 */
public interface IQueries {

    /**
     * Persists a completed simulation run.
     * 
     * @return the generated run id, or -1 if insert failed
     */
    int save(StatisticsAndMetricsRecord record);

    /**
     * Persists all trades from a run in a single transaction.
     * Linked to the run via runId returned by save().
     */
    void saveAll(List<Trade> trades, int runId);

    /**
     * Returns all simulation runs, most recent first.
     */
    List<StatisticsAndMetricsRecord> findAll();

    /**
     * Returns a single run by id, or null if not found.
     */
    StatisticsAndMetricsRecord findById(int id);

    /**
     * Returns all trades belonging to a run, ordered by conclusion time.
     */
    List<TradeRecord> findTradesByRunId(int runId);
}
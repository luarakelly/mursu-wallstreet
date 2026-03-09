package eds.model;

import eds.database.Records.StatisticsAndMetricsRecord;

/**
 * Operations that the controller can call on the simulation engine.
 */
public interface IEngine {
    void setSimulationTime(double time);

    void setDelay(long time);

    long getDelay();

    int[] getQueueLengths();

    StatisticsCollector.Snapshot getStatisticsSnapshot();

    StatisticsAndMetricsRecord getStatisticsRecord();

    void start();

    void interrupt();
}

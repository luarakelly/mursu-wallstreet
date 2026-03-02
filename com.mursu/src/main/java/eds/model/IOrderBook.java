package eds.model;

/**
 * Read-only view of the OrderBook.
 *
 * Anyone outside the matching engine (statisticsCollector and
 * controller) holds this type - they can
 * observe the book state but cannot mutate it.
 */
public interface IOrderBook {
    // Liquidity checks
    boolean hasBids();

    boolean hasAsks();

    /**
     * Immutable snapshot of the full book state.
     * Contains bid/ask depth levels, best prices, mid price and spread.
     */
    OrderBook.OrderBookSnapshot getSnapshot();

    /**
     * Total number of resting orders across both sides.
     * Used by StatisticsCollector at end of simulation.
     */
    int remainingOrderCount();
}

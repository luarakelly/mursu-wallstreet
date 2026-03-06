package eds.model;

/**
 * Read-only access interface for the {@link OrderBook}.
 *
 * <p>
 * This interface exposes query operations that allow external components
 * to observe the state of the order book without modifying it.
 * </p>
 *
 * <p>
 * Typical consumers include controllers, analytics modules, and
 * statistics collectors.
 * </p>
 */
public interface IOrderBook {

    /**
     * Checks whether the order book currently contains any bid orders.
     *
     * @return {@code true} if at least one bid exists, otherwise {@code false}
     */
    boolean hasBids();

    /**
     * Checks whether the order book currently contains any ask orders.
     *
     * @return {@code true} if at least one ask exists, otherwise {@code false}
     */
    boolean hasAsks();

    /**
     * Returns an immutable snapshot of the current order book state.
     *
     * <p>
     * The snapshot contains aggregated bid and ask levels as well as
     * key market metrics such as:
     * </p>
     *
     * <ul>
     * <li>best bid price</li>
     * <li>best ask price</li>
     * <li>mid price</li>
     * <li>bid-ask spread</li>
     * </ul>
     *
     * @return an immutable {@link OrderBook.OrderBookSnapshot} representing
     *         the current state of the book
     */
    OrderBook.OrderBookSnapshot getSnapshot();

    /**
     * Returns the total number of resting orders currently stored
     * in the order book across both bid and ask sides.
     *
     * @return the total count of remaining orders
     */
    int remainingOrderCount();
}

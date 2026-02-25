package eds.model;

import java.util.*;

/**
 * Maintains the resting limit orders on both bid and ask sides.
 *
 * Bids -> sorted descending by price (best bid = highest)
 * Asks -> sorted ascending by price (best ask = lowest)
 *
 * Within the same price level, FIFO ordering is preserved via insertion order.
 */
public class OrderBook {
    // Using TreeMap, because with it insertions automatically go to correct sorted
    // position and we can easily get best price levels via firstKey().

    // Price -> list of resting orders at that level (FIFO within level)
    private final TreeMap<Double, Deque<Order>> asks = new TreeMap<>();
    // By default, TreeMap sorts ascending that is why we need
    // Comparator.reverseOrder(). for bids.
    private final TreeMap<Double, Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
}
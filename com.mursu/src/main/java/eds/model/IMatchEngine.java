package eds.model;

import java.util.List;

/**
 * Stateless domain engine interface for order matching.
 *
 * The matching engine accepts an incoming order, matches it against
 * the OrderBook, and returns all Trade executions produced in that pass.
 *
 * MyEngine calls this once per MARKET_MATCHING_COMPLETE and
 * LIMIT_MATCHING_COMPLETE event.
 */
public interface IMatchEngine {

    /**
     * Matches an incoming order against the book and returns all trades produced.
     *
     * @param incoming    the order incoming the matching service point
     * @param book        the live order book (mutated during matching)
     * @param currentTime simulation clock time at moment of matching
     * @return list of trades executed; empty if no match was possible
     */
    List<Trade> match(Order incoming, OrderBook book, double currentTime);
}

package eds.model;

import java.util.List;

/**
 * Stateless domain service for order matching.
 *
 * <p>
 * The matching engine accepts an incoming {@link Order}, attempts to match
 * it against the current {@link OrderBook}, and produces zero or more
 * {@link Trade} executions.
 * </p>
 *
 * <p>
 * The implementation may modify the provided order book during the
 * matching process and update the state of the involved orders.
 * </p>
 */
public interface IMatchEngine {

    /**
     * Attempts to match an incoming order against the order book.
     *
     * <p>
     * If compatible counterpart orders exist, one or more trades may
     * be generated. The method returns all executions produced during
     * the matching operation.
     * </p>
     *
     * @param incoming    the incoming order to be matched
     * @param book        the active order book used for matching
     * @param currentTime the simulation timestamp at which matching occurs
     *
     * @return a list of {@link Trade} objects representing executed trades;
     *         empty if no matches were possible
     */
    List<Trade> match(Order incoming, OrderBook book, double currentTime);
}
package eds.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Matching engine responsible for executing incoming orders against the
 * current {@link OrderBook}.
 *
 * <p>
 * The engine follows standard price-time priority rules:
 * </p>
 *
 * <ul>
 * <li>Incoming orders are matched against the best available price.</li>
 * <li>Execution price is always the price of the resting order.</li>
 * <li>If an order cannot be fully matched, remaining behavior depends on the
 * order type.</li>
 * </ul>
 *
 * <p>
 * Post-matching behavior:
 * </p>
 *
 * <ul>
 * <li>Remaining {@link Order.Type#LIMIT} orders are added to the order
 * book.</li>
 * <li>Remaining {@link Order.Type#MARKET} orders are cancelled
 * immediately.</li>
 * </ul>
 */
public class MatchEngine implements IMatchEngine {

    /**
     * Processes an incoming order and attempts to match it against the order book.
     *
     * <p>
     * If matching counterpart orders exist, trades are generated until the
     * order is filled or no further matches are possible.
     * </p>
     *
     * @param incoming    the order entering the market
     * @param book        the order book containing resting orders
     * @param currentTime the simulation timestamp of the event
     *
     * @return a list of {@link Trade} objects representing executed trades
     */
    public List<Trade> match(Order incoming, OrderBook book, double currentTime) {

        List<Trade> trades = new ArrayList<>();

        if (incoming.getSide() == Order.Side.BUY) {
            matchBuy(incoming, book, trades, currentTime);
        } else {
            matchSell(incoming, book, trades, currentTime);
        }

        // Handle remainder
        if (incoming.isActive() && incoming.getType() == Order.Type.LIMIT) {
            book.addOrder(incoming);
        } else if (incoming.isActive() && incoming.getType() == Order.Type.MARKET) {
            incoming.cancel(currentTime);
        }

        return trades;
    }

    // ── BUY -match against best asks ────────────────────────────────────────────

    /**
     * Attempts to match an incoming BUY order against resting SELL orders.
     *
     * <p>
     * The engine repeatedly matches the order against the best ask price
     * while the order remains active and matching conditions are satisfied.
     * </p>
     *
     * <p>
     * For {@link Order.Type#LIMIT} orders, execution only occurs if:
     * </p>
     *
     * <pre>
     * buyPrice ≥ bestAskPrice
     * </pre>
     *
     * <p>
     * The execution price is always the price of the resting ask order.
     * </p>
     *
     * @param buy         the incoming buy order
     * @param book        the order book containing resting orders
     * @param trades      the list where generated trades are recorded
     * @param currentTime the simulation timestamp of the trade
     */
    private void matchBuy(Order buy, OrderBook book, List<Trade> trades, double currentTime) {

        while (buy.isActive() && book.hasAsks()) {

            OptionalDouble bestAskPriceOpt = book.getBestAskPrice();
            if (bestAskPriceOpt.isEmpty())
                break;

            double askPrice = bestAskPriceOpt.getAsDouble();

            // LIMIT price check
            if (buy.getType() == Order.Type.LIMIT && buy.getPrice() < askPrice)
                break;

            Optional<Order> restingAskOpt = book.getBestAskOrder();
            if (restingAskOpt.isEmpty())
                break;

            Order restingAsk = restingAskOpt.get();

            int qty = Math.min(
                    buy.getRemainingShareSize(),
                    restingAsk.getRemainingShareSize());

            // Create immutable trade record
            trades.add(new Trade(
                    buy.getId(),
                    restingAsk.getId(),
                    askPrice, // execution price = resting order price
                    qty,
                    currentTime));

            // Update order states
            buy.reduceShareSize(qty, currentTime);
            restingAsk.reduceShareSize(qty, currentTime);

            if (restingAsk.isFilled()) {
                book.removeBestAskOrder();
            }
        }
    }

    // ── SELL -match against best bids ──────────────────────────────────────────

    /**
     * Attempts to match an incoming SELL order against resting BUY orders.
     *
     * <p>
     * The engine repeatedly matches the order against the best bid price
     * while the order remains active and matching conditions are satisfied.
     * </p>
     *
     * <p>
     * For {@link Order.Type#LIMIT} orders, execution only occurs if:
     * </p>
     *
     * <pre>
     * sellPrice ≤ bestBidPrice
     * </pre>
     *
     * <p>
     * The execution price is always the price of the resting bid order.
     * </p>
     *
     * @param sell        the incoming sell order
     * @param book        the order book containing resting orders
     * @param trades      the list where generated trades are recorded
     * @param currentTime the simulation timestamp of the trade
     */
    private void matchSell(Order sell, OrderBook book, List<Trade> trades, double currentTime) {

        while (sell.isActive() && book.hasBids()) {

            OptionalDouble bestBidPriceOpt = book.getBestBidPrice();
            if (bestBidPriceOpt.isEmpty())
                break;

            double bidPrice = bestBidPriceOpt.getAsDouble();

            // LIMIT price check
            if (sell.getType() == Order.Type.LIMIT && sell.getPrice() > bidPrice)
                break;

            Optional<Order> restingBidOpt = book.getBestBidOrder();
            if (restingBidOpt.isEmpty())
                break;

            Order restingBid = restingBidOpt.get();

            int qty = Math.min(
                    sell.getRemainingShareSize(),
                    restingBid.getRemainingShareSize());

            trades.add(new Trade(
                    restingBid.getId(),
                    sell.getId(),
                    bidPrice, // execution price = resting order price
                    qty,
                    currentTime));

            sell.reduceShareSize(qty, currentTime);
            restingBid.reduceShareSize(qty, currentTime);

            if (restingBid.isFilled()) {
                book.removeBestBidOrder();
            }
        }
    }
}

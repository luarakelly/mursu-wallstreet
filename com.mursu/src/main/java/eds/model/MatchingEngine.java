package eds.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * MatchingEngine processes incoming orders against an OrderBook.
 * - Executes trades if matching counterpart orders exist
 * - Routes unfilled LIMIT orders to the resting book
 * - Cancels unfilled MARKET orders immediately
 */
public class MatchingEngine {

    private final double currentTime;

    public MatchingEngine(double currentTime) {
        this.currentTime = currentTime;
    }

    // Main method to process an incoming order
    public List<Trade> match(Order incoming, OrderBook book) {

        List<Trade> trades = new ArrayList<>();

        if (incoming.getSide() == Order.Side.BUY) {
            matchBuy(incoming, book, trades);
        } else {
            matchSell(incoming, book, trades);
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
    private void matchBuy(Order buy, OrderBook book, List<Trade> trades) {
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
    private void matchSell(Order sell, OrderBook book, List<Trade> trades) {
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

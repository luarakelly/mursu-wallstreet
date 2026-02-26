package eds.model;

import java.util.ArrayList;
import java.util.List;

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
        // TODO: implement matching against resting asks using price-time priority
    }

    // ── SELL -match against best bids ──────────────────────────────────────────
    private void matchSell(Order sell, OrderBook book, List<Trade> trades) {
        // TODO: implement matching against resting bids using price-time priority
    }
}

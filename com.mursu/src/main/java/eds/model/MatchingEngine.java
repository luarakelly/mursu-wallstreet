package eds.model;

import java.util.ArrayList;
import java.util.List;

public class MatchingEngine {

    private final double currentTime;

    public MatchingEngine(double currentTime) {
        this.currentTime = currentTime;
    }

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

    // ── BUY - match against best asks ───────────────────────────────────────
    private void matchBuy(Order buy, OrderBook book, List<Trade> trades) {
        // TODO: implement this method

    }

    // ── SELL - match against best bids ──────────────────────────────────────
    private void matchSell(Order sell, OrderBook book, List<Trade> trades) {
        // TODO: implement this method
    }
}

package controller;

import eds.model.OrderBook;

/**
 * Operations that the engine can call on the controller.
 */
public interface IModelToViewController {
    void showEndTime(double time);

    void updateTimeAndQueues();

    void updateOrderBook(OrderBook.OrderBookSnapshot snapshot);
}

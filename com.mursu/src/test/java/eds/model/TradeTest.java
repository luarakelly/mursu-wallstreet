package eds.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Trade tests")
class TradeTest {

    @Test
    @DisplayName("Should create valid trade")
    void shouldCreateValidTrade() {
        Trade trade = new Trade("BUY_ID", "SELL_ID", 100.50, 10, 5.50);

        assertNotNull(trade.getId());
        assertEquals("BUY_ID", trade.getBuyOrderId());
        assertEquals("SELL_ID", trade.getSellOrderId());
        assertEquals(100.50, trade.getPrice());
        assertEquals(10, trade.getShareSize());
        assertEquals(5.50, trade.getConclusionTime());
    }

    @Test
    @DisplayName("Should throw an error if buy and sell order IDs are equal")
    void shouldThrowIfOrderIdsAreEqual() {
        assertThrows(IllegalArgumentException.class, () ->
                new Trade("BUY_ID", "BUY_ID", 100.50, 10, 5.50)
        );
    }

    @Test
    @DisplayName("Should throw an error if price is less than or equal to zero")
    void shouldThrowIfPriceIsNonPositive() {
        assertThrows(IllegalArgumentException.class, () ->
                new Trade("BUY_ID", "SELL_ID", 0.0, 10, 5.50)
        );
    }

    @Test
    @DisplayName("Should throw an error if share size is less than or equal to zero")
    void shouldThrowIfShareSizeIsNonPositive() {
        assertThrows(IllegalArgumentException.class, () ->
                new Trade("BUY_ID", "SELL_ID", 100.50, 0, 5.50)
        );
    }

    @Test
    @DisplayName("Should contain key trade data")
    void toStringShouldContainKeyInformation() {
        Trade trade = new Trade("BUY_ID", "SELL_ID", 100.50, 10, 5.50);

        String output = trade.toString();

        assertTrue(output.contains("BUY_ID"));
        assertTrue(output.contains("SELL_ID"));
        assertTrue(output.contains("100.50"));
        assertTrue(output.contains("10"));
        assertTrue(output.contains("5.50"));
    }
}
package ru.pepe.autosell;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TradeAvailabilityTest {
    @Test void mixedStockIsGreenUntilAllEmeraldTradesAreExhausted() {
        assertEquals(TradeAvailability.AVAILABLE, TradeAvailability.fromCounts(4, 1));
        assertEquals(TradeAvailability.EXHAUSTED, TradeAvailability.fromCounts(4, 0));
    }
    @Test void restockBecomesGreenOnlyWithNewPositiveStock() {
        assertEquals(TradeAvailability.EXHAUSTED, TradeAvailability.fromCounts(1, 0));
        assertEquals(TradeAvailability.AVAILABLE, TradeAvailability.fromCounts(1, 1));
    }
    @Test void unknownAndNoEmeraldTradesHaveNoArtificialGlow() {
        assertEquals(0, TradeAvailability.UNKNOWN.color);
        assertEquals(0, TradeAvailability.fromCounts(0, 0).color);
        assertEquals(0xFF32FF65, TradeAvailability.AVAILABLE.color);
        assertEquals(0xFFFF3636, TradeAvailability.EXHAUSTED.color);
    }
}

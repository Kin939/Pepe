package ru.pepe.autosell;

/** Availability after checking server stock and payable inventory inputs. */
enum TradeAvailability {
    UNKNOWN(0), NONE(0), AVAILABLE(0xFF32FF65), EXHAUSTED(0xFFFF3636);
    final int color;
    TradeAvailability(int color) { this.color = color; }
    static TradeAvailability fromCounts(int emeraldOffers, int availableOffers) {
        if (emeraldOffers <= 0) return NONE;
        return availableOffers > 0 ? AVAILABLE : EXHAUSTED;
    }
}

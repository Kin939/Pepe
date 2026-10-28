package ru.pepe.autosell;

/** Convert a confirmed bulk output to individual completed exchanges. */
final class BatchResult {
    static int tradeCount(int emeralds, int outputPerTrade) {
        if (emeralds <= 0 || outputPerTrade <= 0) return 0;
        return emeralds / outputPerTrade;
    }
}

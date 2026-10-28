package ru.pepe.autosell;

/** Earnings are independent of the current inventory balance and item spending. */
final class SessionEarnings {
    private long total;
    long total() { return total; }
    int add(int earned) {
        if (earned <= 0) return 0;
        long oldStacks = total / 64;
        total = Math.addExact(total, earned);
        return Math.toIntExact(total / 64 - oldStacks);
    }
    void reset() { total = 0; }
}

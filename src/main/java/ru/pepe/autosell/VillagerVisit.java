package ru.pepe.autosell;

/** One completed, profitable interaction earns at most one combo, never one per trade. */
final class VillagerVisit {
    private boolean sold, finished;
    void sold() { sold = true; }
    boolean finish() {
        if (finished) return false;
        finished = true;
        return sold;
    }
}

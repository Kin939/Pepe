package ru.pepe.autosell;

/** A sale advances one step; the sale after eight starts a fresh sequence. */
final class ComboCounter {
    private int value;
    int next() {
        int shown = ++value;
        if (value == 8) value = 0;
        return shown;
    }
    void reset() { value = 0; }
}

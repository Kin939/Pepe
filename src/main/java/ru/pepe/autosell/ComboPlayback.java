package ru.pepe.autosell;

/** Presentation queue: never delays selling, each queued step represents one completed profitable villager visit. */
final class ComboPlayback {
    private final ComboCounter counter = new ComboCounter();
    private int pending, cooldown;
    void add(int visits) { if (visits > 0) pending = Math.addExact(pending, visits); }
    int tick() {
        if (cooldown > 0) { cooldown--; return 0; }
        if (pending == 0) return 0;
        pending--;
        int step = counter.next();
        cooldown = switch (step) {
            case 7 -> 19; // one-second recording
            case 8 -> 27; // let the finale and rainbow remain visible
            default -> 8;
        };
        return step;
    }
    void clear() { pending = cooldown = 0; counter.reset(); }
}

package ru.pepe.autosell;

/** Event-only camera accent. Neutral when idle; no continuous FOV oscillation. */
final class ImpactZoom {
    private int remaining, duration;
    private float strength;
    void trigger(boolean finale) {
        duration = remaining = finale ? 22 : 14;
        strength = finale ? 0.035f : 0.02f;
    }
    void tick() { if (remaining > 0) remaining--; }
    float multiplier() {
        return remaining == 0 ? 1 : 1 - strength * (float) Math.sin(Math.PI * (duration - remaining) / duration);
    }
    void clear() { remaining = duration = 0; strength = 0; }
}

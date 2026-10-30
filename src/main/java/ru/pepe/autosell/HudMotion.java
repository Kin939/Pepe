package ru.pepe.autosell;

/** Small deterministic animation helpers, independent from Minecraft and trading. */
final class HudMotion {
    static float clamp(float x) { return Math.max(0, Math.min(1, x)); }
    static float easeOut(float x) { float t = 1 - clamp(x); return 1 - t * t * t; }
    static float hudScale(int width, int height) {
        return Math.max(0.55f, Math.min(1f, Math.min(width / 480f, height / 300f)));
    }
    static double counterStep(double current, long target) {
        if (Math.abs(target - current) < 0.5) return target;
        return current + (target - current) * 0.24;
    }
    static int alpha(float opacity) { return Math.round(255 * clamp(opacity)); }
    static int tint(int rgb, float opacity) { return (alpha(opacity) << 24) | (rgb & 0xFFFFFF); }
}

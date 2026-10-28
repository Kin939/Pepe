package ru.pepe.autosell;

/** Pure screen-space animation, with no entity/model renderer dependencies. */
final class FlybyMotion {
    static final int LIFETIME = 36;
    static float progress(int age) { return Math.clamp(age / (float) LIFETIME, 0f, 1f); }
    static float x(int age, int width) { return -140 + progress(age) * (width + 280); }
    static float angle(int age) { return progress(age) * (float) (Math.PI * 4); }
}

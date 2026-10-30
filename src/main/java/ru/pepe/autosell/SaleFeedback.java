package ru.pepe.autosell;

/** Pure visual state: never sends packets or changes player options. */
final class SaleFeedback {
    enum Emotion {
        IDLE("idle", 0, 0xFF51D99A), SUCCESS("success", 1, 0xFF6BFFA0),
        FAILURE("failure", 1, 0xFFFF6969), STACK("stack", 2, 0xFF5CF4E5),
        FINALE("finale", 3, 0xFFFFD75E);
        final String texture;
        final int priority, color;
        Emotion(String texture, int priority, int color) {
            this.texture = texture; this.priority = priority; this.color = color;
        }
    }
    private Emotion emotion = Emotion.IDLE;
    private int emotionTicks, flashTicks;
    private boolean failureFlash;
    Emotion emotion() { return emotion; }
    void sold(boolean crossedStack) {
        flashTicks = 20;
        failureFlash = false;
        react(crossedStack ? Emotion.STACK : Emotion.SUCCESS, crossedStack ? 60 : 35);
    }
    void failed() {
        flashTicks = 20;
        failureFlash = true;
        react(Emotion.FAILURE, 40);
    }
    void finale() { react(Emotion.FINALE, 100); }
    private void react(Emotion next, int duration) {
        if (emotionTicks == 0 || next.priority >= emotion.priority) {
            emotion = next;
            emotionTicks = duration;
        }
    }
    void tick() {
        if (flashTicks > 0) flashTicks--;
        if (emotionTicks > 0 && --emotionTicks == 0) emotion = Emotion.IDLE;
    }
    int flashColor() {
        // At most 6% opacity, no additive build-up during bulk selling.
        int alpha = Math.round(15f * flashTicks / 20f);
        return alpha == 0 ? 0 : (alpha << 24) | (failureFlash ? 0xF04747 : 0x36E886);
    }
    static float zoomMultiplier(double seconds) {
        // One gentle breathing cycle per five seconds, +/- 1.25% of the current FOV.
        return 1f + 0.0125f * (float) Math.sin(seconds * Math.PI * 2 / 5);
    }
    void clear() {
        emotion = Emotion.IDLE;
        emotionTicks = flashTicks = 0;
        failureFlash = false;
    }
}

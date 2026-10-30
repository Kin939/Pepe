package ru.pepe.autosell;

final class PortraitArt {
    static final String[] NAMES = {"idle","blink","focus","success","grin","wink","laugh","proud",
            "failure","angry","sad","shocked","stack","amazed","rich","finale","ecstatic","jackpot"};
    static String name(SaleFeedback.Emotion emotion, long clock, int event) {
        return switch (emotion) {
            case IDLE -> clock % 110 < 4 ? "blink" : clock % 180 > 140 ? "focus" : "idle";
            case SUCCESS -> NAMES[3 + Math.floorMod(event + (int)(clock / 16), 5)];
            case FAILURE -> NAMES[8 + Math.floorMod(event + (int)(clock / 24), 4)];
            case STACK -> NAMES[12 + (int)(clock / 15 % 3)];
            case FINALE -> NAMES[15 + (int)(clock / 12 % 3)];
        };
    }
}

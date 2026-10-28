package ru.pepe.autosell;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BatchResultTest {
    @Test void twelveEmeraldFarmerTradesCountAsTwelve() {
        assertEquals(12, BatchResult.tradeCount(12, 1));
    }
    @Test void countsExchangesNotEmeraldsForMultiEmeraldOutputs() {
        assertEquals(6, BatchResult.tradeCount(12, 2));
        assertEquals(0, BatchResult.tradeCount(0, 1));
        assertEquals(0, BatchResult.tradeCount(12, 0));
    }
    @Test void allTwelveSuccessfulVisitsSurviveInstantQueue() {
        ComboPlayback playback = new ComboPlayback();
        playback.add(12);
        var result = new java.util.ArrayList<Integer>();
        for (int tick = 0; tick < 250; tick++) {
            int step = playback.tick();
            if (step > 0) result.add(step);
        }
        assertEquals(java.util.List.of(1,2,3,4,5,6,7,8,1,2,3,4), result);
    }
    @Test void disablingClearsQueuedSoundsAndCounter() {
        ComboPlayback playback = new ComboPlayback();
        playback.add(12);
        assertEquals(1, playback.tick());
        playback.clear();
        for (int tick = 0; tick < 100; tick++) assertEquals(0, playback.tick());
        playback.add(1);
        assertEquals(1, playback.tick());
    }
}

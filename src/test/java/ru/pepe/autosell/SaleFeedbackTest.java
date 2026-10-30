package ru.pepe.autosell;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SaleFeedbackTest {
    @Test void successAndFailureFlashForOneSecondWithoutAccumulatingOpacity() {
        var state = new SaleFeedback();
        assertEquals(0, state.flashColor());
        state.sold(false);
        assertEquals(SaleFeedback.Emotion.SUCCESS, state.emotion());
        assertEquals(15, state.flashColor() >>> 24);
        for (int i = 0; i < 10; i++) state.sold(false);
        assertEquals(15, state.flashColor() >>> 24);
        state.failed();
        assertEquals(0xF04747, state.flashColor() & 0xFFFFFF);
        assertEquals(SaleFeedback.Emotion.FAILURE, state.emotion());
        for (int i = 0; i < 20; i++) state.tick();
        assertEquals(0, state.flashColor());
    }
    @Test void celebrationsHavePriorityOverBulkSalesThenReturnToIdle() {
        var state = new SaleFeedback();
        state.sold(true);
        state.sold(false);
        assertEquals(SaleFeedback.Emotion.STACK, state.emotion());
        state.finale();
        state.failed();
        assertEquals(SaleFeedback.Emotion.FINALE, state.emotion());
        for (int i = 0; i < 100; i++) state.tick();
        assertEquals(SaleFeedback.Emotion.IDLE, state.emotion());
    }
    @Test void clearResetsFlashAndPortraitImmediately() {
        var state = new SaleFeedback();
        state.failed();
        state.finale();
        state.clear();
        assertEquals(0, state.flashColor());
        assertEquals(SaleFeedback.Emotion.IDLE, state.emotion());
    }
    @Test void gentleZoomHasFiveSecondPeriodAndBoundedAmplitude() {
        assertEquals(1f, SaleFeedback.zoomMultiplier(0), 0.00001);
        assertEquals(1.0125f, SaleFeedback.zoomMultiplier(1.25), 0.00001);
        assertEquals(0.9875f, SaleFeedback.zoomMultiplier(3.75), 0.00001);
        for (int i = 0; i < 200; i++) {
            double time = i * 0.037;
            float f = SaleFeedback.zoomMultiplier(time);
            assertTrue(f >= 0.98749f && f <= 1.01251f);
            assertEquals(f, SaleFeedback.zoomMultiplier(time + 5), 0.00001);
        }
    }
}

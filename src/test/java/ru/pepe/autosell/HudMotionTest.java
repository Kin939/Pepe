package ru.pepe.autosell;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HudMotionTest {
    @Test void easeOutIsClampedAndMonotonic() {
        assertEquals(0, HudMotion.easeOut(-5));
        assertEquals(1, HudMotion.easeOut(5));
        float previous = 0;
        for (int i = 0; i <= 100; i++) {
            float next = HudMotion.easeOut(i / 100f);
            assertTrue(next >= previous && next <= 1);
            previous = next;
        }
    }
    @Test void animatedCounterNeverOvershootsAndEventuallySettles() {
        double shown = 0;
        for (int i = 0; i < 100; i++) {
            double next = HudMotion.counterStep(shown, 129);
            assertTrue(next >= shown && next <= 129);
            shown = next;
        }
        assertEquals(129, shown);
        assertEquals(0, HudMotion.counterStep(0, 0));
    }
    @Test void hudFitsSmallScreensAndDoesNotGrowWithoutLimit() {
        assertEquals(1f, HudMotion.hudScale(1920, 1080));
        float scale = HudMotion.hudScale(320, 180);
        assertTrue(scale >= 0.55f && scale < 0.7f);
        assertTrue(128 * scale + 36 < 180);
        assertEquals(0, HudMotion.alpha(-1));
        assertEquals(255, HudMotion.alpha(4));
        assertEquals(0x8064FFD0, HudMotion.tint(0xFF64FFD0, 0.5f));
    }
    @Test void newHudIsBrandedAndRemainsGuiOnly() throws Exception {
        try (var stream = getClass().getResourceAsStream("/ru/pepe/autosell/HypeHud.class")) {
            assertNotNull(stream);
            String constants = new String(stream.readAllBytes(), StandardCharsets.ISO_8859_1);
            assertTrue(constants.contains("HYPE TV"));
            assertFalse(constants.contains("PEPE TV"));
            assertFalse(constants.contains("net/minecraft/world/entity/"));
            assertFalse(constants.contains("InventoryScreen"));
        }
    }
}

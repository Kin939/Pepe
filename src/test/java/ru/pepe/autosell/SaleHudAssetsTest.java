package ru.pepe.autosell;

import javax.imageio.ImageIO;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SaleHudAssetsTest {
    @Test void portraitsAreDistinctBundledPixelArt() throws Exception {
        var hashes = new HashSet<Integer>();
        for (var emotion : SaleFeedback.Emotion.values()) {
            try (var stream = getClass().getResourceAsStream("/assets/pepe/textures/gui/face_" + emotion.texture + ".png")) {
                assertNotNull(stream);
                byte[] bytes = stream.readAllBytes();
                hashes.add(java.util.Arrays.hashCode(bytes));
                var image = ImageIO.read(new java.io.ByteArrayInputStream(bytes));
                assertEquals(64, image.getWidth());
                assertEquals(64, image.getHeight());
            }
        }
        assertEquals(5, hashes.size());
    }
    @Test void vignetteLeavesTheCenterCompletelyTransparent() throws Exception {
        try (var stream = getClass().getResourceAsStream("/assets/pepe/textures/gui/emerald_vignette.png")) {
            assertNotNull(stream);
            var image = ImageIO.read(stream);
            assertEquals(256, image.getWidth());
            assertEquals(256, image.getHeight());
            assertEquals(0, image.getRGB(128, 128) >>> 24);
            assertTrue(image.getRGB(0, 0) >>> 24 > 50);
            assertTrue(image.getRGB(0, 128) >>> 24 < 100);
        }
    }
    @Test void oldHardCodedHudLabelIsGone() throws Exception {
        try (var stream = getClass().getResourceAsStream("/ru/pepe/autosell/Effects.class")) {
            assertNotNull(stream);
            String constants = new String(stream.readAllBytes(), StandardCharsets.ISO_8859_1);
            assertFalse(constants.contains("AUTO SELL [C]"));
            assertTrue(constants.contains("textures/gui/emerald_vignette.png"));
        }
    }
}
